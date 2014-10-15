package com.asigbe.preference;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Debug;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.asigbe.gwakeup.R;
import com.asigbe.gwakeup.SettingsActivity;

/**
 * @hide
 */
public class VolumePreference extends SeekBarPreference implements
		PreferenceManager.OnActivityStopListener, View.OnKeyListener {

	private static final String TAG = "VolumePreference";

	/** May be null if the dialog isn't visible. */
	private SeekBarVolumizer seekBarVolumizer;

	private AudioManager audioManager;

	private int defaultValue;

	private int restoredValue;

	public VolumePreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.audioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		this.defaultValue = this.audioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);

		final SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekbar);
		this.seekBarVolumizer = new SeekBarVolumizer(getContext(), seekBar);
		this.seekBarVolumizer.lastProgress = this.restoredValue;
		seekBar.setProgress(this.seekBarVolumizer.lastProgress);

		// grab focus and key events so that pressing the volume buttons in the
		// dialog doesn't also show the normal volume adjust toast.
		view.setOnKeyListener(this);
		view.setFocusableInTouchMode(true);
		view.requestFocus();
	}

	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// If key arrives immediately after the activity has been cleaned up.
		if (this.seekBarVolumizer == null) {
			return true;
		}
		boolean isdown = (event.getAction() == KeyEvent.ACTION_DOWN);
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if (isdown) {
				this.seekBarVolumizer.changeVolumeBy(-1);
			}
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			if (isdown) {
				this.seekBarVolumizer.changeVolumeBy(1);
			}
			return true;
		default:
			return false;
		}
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		persistInt(this.seekBarVolumizer.lastProgress);

		cleanup();
	}

	@Override
	public void onActivityStop() {
		cleanup();
	}

	/**
	 * Do clean up. This can be called multiple times!
	 */
	public void cleanup() {
		if (this.seekBarVolumizer != null) {
			Dialog dialog = getDialog();
			if ((dialog != null) && dialog.isShowing()) {
				View view = dialog.getWindow().getDecorView().findViewById(
						R.id.seekbar);
				if (view != null) {
					view.setOnKeyListener(null);
				}
			}
			// Stopped while dialog was showing, revert changes
			this.seekBarVolumizer.revertVolume();
			this.seekBarVolumizer.stop();
			this.seekBarVolumizer = null;
		}

	}

	protected void onSampleStarting(SeekBarVolumizer volumizer) {
		if ((this.seekBarVolumizer != null)
				&& (volumizer != this.seekBarVolumizer)) {
			this.seekBarVolumizer.stopSample();
		}
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		if (isPersistent()) {
			// No need to save instance state since it's persistent
			return superState;
		}

		final SavedState myState = new SavedState(superState);
		if (this.seekBarVolumizer != null) {
			this.seekBarVolumizer.onSaveInstanceState(myState.getVolumeStore());
		}
		return myState;
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		this.restoredValue = ((restoreValue ? getPersistedInt(this.defaultValue)
				: this.defaultValue));
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if ((state == null) || !state.getClass().equals(SavedState.class)) {
			// Didn't save state for us in onSaveInstanceState
			super.onRestoreInstanceState(state);
			return;
		}

		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());
		if (this.seekBarVolumizer != null) {
			this.seekBarVolumizer.onRestoreInstanceState(myState
					.getVolumeStore());
		}
	}

	public static class VolumeStore {
		public int volume = -1;
		public int originalVolume = -1;
	}

	private static class SavedState extends BaseSavedState {
		VolumeStore mVolumeStore = new VolumeStore();

		public SavedState(Parcel source) {
			super(source);
			this.mVolumeStore.volume = source.readInt();
			this.mVolumeStore.originalVolume = source.readInt();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(this.mVolumeStore.volume);
			dest.writeInt(this.mVolumeStore.originalVolume);
		}

		VolumeStore getVolumeStore() {
			return this.mVolumeStore;
		}

		public SavedState(Parcelable superState) {
			super(superState);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

	/**
	 * Turns a {@link SeekBar} into a volume control.
	 */
	public class SeekBarVolumizer implements OnSeekBarChangeListener, Runnable {

		private final Context mContext;
		private final Handler handler = new Handler();

		private final AudioManager audioManager;
		private int originalStreamVolume;
		private Ringtone ringtone;

		private int lastProgress = -1;
		private final SeekBar seekBar;

		public SeekBarVolumizer(Context context, SeekBar seekBar) {
			this.mContext = context;
			this.audioManager = (AudioManager) context
					.getSystemService(Context.AUDIO_SERVICE);
			this.seekBar = seekBar;

			initSeekBar(seekBar);
		}

		private void initSeekBar(SeekBar seekBar) {
			seekBar.setMax(this.audioManager
					.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

			// Get the settings preferences
			this.originalStreamVolume = this.audioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC);

			seekBar.setOnSeekBarChangeListener(this);

			Uri defaultUri = null;
			defaultUri = Settings.System.DEFAULT_RINGTONE_URI;

			this.ringtone = RingtoneManager.getRingtone(this.mContext,
					defaultUri);
			if (this.ringtone != null) {
				this.ringtone.setStreamType(AudioManager.STREAM_MUSIC);
			}
		}

		public void stop() {
			stopSample();
			this.seekBar.setOnSeekBarChangeListener(null);
		}

		public void revertVolume() {
			this.audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
					this.originalStreamVolume, 0);
		}

		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromTouch) {
			if (!fromTouch) {
				return;
			}

			postSetVolume(progress);
		}

		void postSetVolume(int progress) {
			// Do the volume changing separately to give responsive UI
			this.lastProgress = progress;

			this.handler.removeCallbacks(this);
			this.handler.post(this);
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
			if ((this.ringtone != null) && !this.ringtone.isPlaying()) {
				sample();
			}
		}

		public void run() {
			this.audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
					this.lastProgress, 0);
		}

		private void sample() {
			onSampleStarting(this);
			this.ringtone.play();
		}

		public void stopSample() {
			if (this.ringtone != null) {
				this.ringtone.stop();
			}
		}

		public SeekBar getSeekBar() {
			return this.seekBar;
		}

		public void changeVolumeBy(int amount) {
			this.seekBar.incrementProgressBy(amount);
			if ((this.ringtone != null) && !this.ringtone.isPlaying()) {
				sample();
			}
			postSetVolume(this.seekBar.getProgress());
		}

		public void onSaveInstanceState(VolumeStore volumeStore) {
			if (this.lastProgress >= 0) {
				volumeStore.volume = this.lastProgress;
				volumeStore.originalVolume = this.originalStreamVolume;
			}
		}

		public void onRestoreInstanceState(VolumeStore volumeStore) {
			if (volumeStore.volume != -1) {
				this.originalStreamVolume = volumeStore.originalVolume;
				this.lastProgress = volumeStore.volume;
				this.seekBar.setProgress(this.lastProgress);
				postSetVolume(this.lastProgress);
			}
		}
	}
}
