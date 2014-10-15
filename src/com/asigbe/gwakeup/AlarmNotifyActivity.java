package com.asigbe.gwakeup;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.asigbe.hardware.MovementDetector;
import com.asigbe.hardware.ShakingMovement;
import com.asigbe.sensor.Movement;
import com.asigbe.sensor.OnMovementListener;
import com.asigbe.view.ViewTools;

/**
 * This activity is launch when the alarm is triggered. It plays the music and
 * allows to stop it or snooze.
 * 
 */
public class AlarmNotifyActivity extends Activity implements
		MediaPlayer.OnCompletionListener {

	private static final int NOTIFY_DIALOG = 0;

	private final Random random = new Random();

	private View dialogView;
	private MediaPlayer mediaPlayer;
	private boolean vibrate;
	private boolean isSnooze;
	private KeyguardLock keyguardLock;
	private int snooze;
	private WakeLock wakeLock;
	private String name;
	private MovementDetector movementDetector;
	private boolean canSnoozeByShaking;
	private int alarmVolume;
	private int originalStreamVolume;
	private AudioManager audioManager;
	private String[] stringArrayExtras;
	private int fading;

	private Intent lastIntent;

	private boolean hasFinishedPlaying;

	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);

		switch (id) {
		case NOTIFY_DIALOG:
			Dialog dialog = new Dialog(this);
			dialog.setTitle(this.name);
			this.dialogView = ViewTools.inflateView(this,
					R.layout.alarmnotificationview);
			Button stopButton = (Button) this.dialogView
					.findViewById(R.id.stopButton);
			stopButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					stopAlarm();
				}
			});
			Button snoozeButton = (Button) this.dialogView
					.findViewById(R.id.snoozeButton);
			snoozeButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					snooze();
				}
			});
			dialog.setContentView(this.dialogView);
			return dialog;
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);

		switch (id) {
		case NOTIFY_DIALOG:
			dialog.setTitle(this.name);
			break;
		default:
			break;
		}
	}

	private void stopAlarm() {
		this.isSnooze = false;

		finish();
	}

	private void snooze() {
		this.isSnooze = true;

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.MINUTE, this.snooze);

		Intent intent = new Intent(this, AlarmNotifyActivity.class)
				.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtras(this.lastIntent.getExtras());

		PendingIntent sender = PendingIntent.getActivity(this, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTime().getTime(),
				sender);

		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		this.originalStreamVolume = this.audioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		loadSettings();

		// configure phone state
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		tm.listen(new PhoneStateListener() {
			public void onCallStateChanged(int state, String incomingNumber) {
				manageCallState(state);
			}
		}, PhoneStateListener.LISTEN_CALL_STATE);

		this.lastIntent = getIntent();
		playAlarm();
		AlarmScheduler.scheduleAlarm(this, false, new Date());
	}

	private void manageCallState(int state) {
		switch (state) {
		case TelephonyManager.CALL_STATE_RINGING:
		case TelephonyManager.CALL_STATE_OFFHOOK:
			try {
				if (this.mediaPlayer != null && this.mediaPlayer.isPlaying()) {
					this.mediaPlayer.pause();
				}
			} catch (IllegalStateException e) {
			}
			break;
		case TelephonyManager.CALL_STATE_IDLE:
			try {
				if (this.mediaPlayer != null && !this.hasFinishedPlaying) {
					this.mediaPlayer.start();
					this.mediaPlayer.setOnCompletionListener(this);
				}
			} catch (IllegalStateException e) {
			}
			break;
		default:
			break;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		this.audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		this.originalStreamVolume = this.audioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		this.lastIntent = intent;
		playAlarm();
	}

	private void loadSettings() {
		// Get the settings preferences
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		this.canSnoozeByShaking = sp.getBoolean(
				SettingsActivity.SNOOZE_SHAKE_KEY, true);
		this.alarmVolume = sp.getInt(SettingsActivity.ALARM_VOLUME_KEY, 100);
	}

	private void playAlarm() {

		// Lock the power
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		this.wakeLock = powerManager
				.newWakeLock(PowerManager.FULL_WAKE_LOCK
						| PowerManager.ACQUIRE_CAUSES_WAKEUP,
						"AlarmNotifyActivityWake");
		this.wakeLock.acquire();

		// Unlock the keyboard
		KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		this.keyguardLock = keyguardManager
				.newKeyguardLock("AlarmNotifiyActivity");
		this.keyguardLock.disableKeyguard();

		this.stringArrayExtras = this.lastIntent.getStringArrayExtra("musics");
		this.fading = this.lastIntent.getIntExtra("fading", 0);
		this.name = this.lastIntent.getStringExtra("name");
		// to prevent crash with alarms created with the previous version of
		// GWakeUp
		if (this.name == null) {
			this.name = "";
		}

		this.vibrate = this.lastIntent.getBooleanExtra("vibrate", false);
		this.snooze = this.lastIntent.getIntExtra("snooze", 10);

		this.audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
				this.alarmVolume, 0);
		if (this.mediaPlayer != null) {
			this.mediaPlayer.stop();
			this.mediaPlayer.release();
		}

		boolean mustSetDefaultAlarm = true;
		if (this.stringArrayExtras.length > 0) {
			this.mediaPlayer = new MediaPlayer();
			try {
				this.mediaPlayer
						.setDataSource(this.stringArrayExtras[this.random
								.nextInt(this.stringArrayExtras.length)]);
			} catch (IllegalArgumentException e) {
			} catch (IllegalStateException e) {
			} catch (IOException e) {
			}

			try {
				this.mediaPlayer.prepare();
				mustSetDefaultAlarm = false;
			} catch (IllegalStateException e) {
			} catch (IOException e) {
			}
		}

		if (mustSetDefaultAlarm || this.mediaPlayer == null) {
			this.mediaPlayer = MediaPlayer.create(this, R.raw.default_sound);
		}

		if (this.mediaPlayer == null) {
			// if sound is null, we force vibration
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(new long[] { 400, 450, 850, 900, 1500 }, 0);
		} else {
			if (this.fading > 0) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(System.currentTimeMillis());

				final Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					private float volume = 0;

					@Override
					public void run() {
						try {
							if (AlarmNotifyActivity.this.mediaPlayer
									.isPlaying()) {
								AlarmNotifyActivity.this.mediaPlayer.setVolume(
										this.volume, this.volume);
							}
						} catch (IllegalStateException e) {
						}
						this.volume += 0.1 / fading;
						if (this.volume > 1) {
							timer.cancel();
						}
					}
				}, calendar.getTime(), 100);
			}

			TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
			this.hasFinishedPlaying = false;
			if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
				this.mediaPlayer.start();
				this.mediaPlayer.setOnCompletionListener(this);
			}
		}

		if (this.vibrate && !this.canSnoozeByShaking) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(new long[] { 400, 450, 850, 900, 1500 }, 0);
		}
		if (this.canSnoozeByShaking) {
			this.movementDetector = new MovementDetector(this,
					new ShakingMovement());
			this.movementDetector
					.setOnMovementListener(new OnMovementListener() {

						@Override
						public void movementDetected(int type) {
							if (type == Movement.SHAKING_MOVEMENT) {
								snooze();
							}
						}
					});
		}
		showDialog(NOTIFY_DIALOG);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		AlarmNotifyActivity.this.hasFinishedPlaying = true;
	}

	@Override
	public void onResume() {
		if (this.movementDetector != null) {
			this.movementDetector.resume();
		}

		super.onResume();
	}

	@Override
	public void onPause() {
		if (this.movementDetector != null) {
			this.movementDetector.pause();
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		this.keyguardLock.reenableKeyguard();
		this.mediaPlayer.release();
		if (this.vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.cancel();
		}

		if (!this.isSnooze) {
			AlarmScheduler.scheduleAlarm(this, true, new Date());
		}

		this.wakeLock.release();
		this.audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
				this.originalStreamVolume, 0);
		super.onStop();
	}
}
