package com.asigbe.gwakeup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Audio.AudioColumns;
import android.text.Spannable;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

public class AlarmConfigurationActivity extends Activity implements
		OnClickListener {

	public static final String EXTRA_MUSICS = "extra_musics";
	private boolean is24HourView;
	private List<AlarmClock> alarmClocks = new ArrayList<AlarmClock>();
	private List<Song> currentSongs = new ArrayList<Song>();
	private TreeSet<String> currentMusicPaths = new TreeSet<String>();
	private TextView textSwitcher;
	private static Song DEFAULT_SONG;
	private Song currentSong;

	// Create the runnable used to switch between different title
	final Runnable runnable = new Runnable() {

		@Override
		public void run() {
			AlarmConfigurationActivity.this.textSwitcher.setText(
					AlarmConfigurationActivity.this.currentSong
							.getformattedDescription(),
					TextView.BufferType.SPANNABLE);
			configureSpannable(
					(Spannable) AlarmConfigurationActivity.this.textSwitcher
							.getText(),
					AlarmConfigurationActivity.this.currentSong);
			AlarmConfigurationActivity.this.textSwitcher.postInvalidate();
			AlarmConfigurationActivity.this.textSwitcher = ((TextView) findViewById(R.id.musicSwitcher));

		}
	};
	private AlarmClock alarmClock;
	private Button saveButton;
	private Button cancelButton;
	private Date closestAlarm;
	private CheckBox enableCheckBox;
	private CheckBox fadingCheckBox;
	private EditText fadingEditText;
	private TimePicker timePicker;
	private DateTimePicker dateTimePicker;
	private EditText snoozeEditText;
	private CheckBox vibrateCheckBox;
	private EditText nameEditText;
	private Button browseButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.alarmconfigurationview);

		AlarmConfigurationActivity.DEFAULT_SONG = new Song("", "",
				getResources().getString(R.string.defaultMusic));
		this.currentSong = AlarmConfigurationActivity.DEFAULT_SONG;
		this.is24HourView = DateFormat.is24HourFormat(this);

		this.enableCheckBox = ((CheckBox) findViewById(R.id.enableCheckBox));
		this.enableCheckBox.setOnClickListener(this);

		this.fadingCheckBox = (CheckBox) findViewById(R.id.fadingCheckBox);
		this.fadingEditText = (EditText) findViewById(R.id.fadingEditText);
		this.fadingCheckBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlarmConfigurationActivity.this.fadingEditText
						.setEnabled(AlarmConfigurationActivity.this.fadingCheckBox
								.isChecked());
			}
		});

		// it's not the right place

		this.textSwitcher = ((TextView) findViewById(R.id.musicSwitcher));
		this.textSwitcher.setText(this.currentSong.getformattedDescription(),
				TextView.BufferType.SPANNABLE);
		configureSpannable((Spannable) this.textSwitcher.getText(),
				this.currentSong);

		// launch the timer
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			private int index = 0;

			@Override
			public void run() {
				synchronized (AlarmConfigurationActivity.this.currentSongs) {
					if (this.index >= AlarmConfigurationActivity.this.currentSongs
							.size()) {
						this.index = 0;
					}
					if (AlarmConfigurationActivity.this.currentSongs.size() > 0) {
						AlarmConfigurationActivity.this.currentSong = AlarmConfigurationActivity.this.currentSongs
								.get(this.index);
						runOnUiThread(AlarmConfigurationActivity.this.runnable);
						this.index++;
					}
				}
			}
		}, calendar.getTime(), 2000);

		this.browseButton = (Button) findViewById(R.id.browseButton);
		this.browseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showDialog(AUDIO_DIALOG);
				Intent intent = new Intent(getApplicationContext(),
						AlbumActivity.class);
				intent.putExtra(AlarmConfigurationActivity.EXTRA_MUSICS,
						AlarmConfigurationActivity.this.currentMusicPaths);
				startActivityForResult(intent, 0);
			}
		});
		this.saveButton = (Button) findViewById(R.id.saveButton);
		this.saveButton.setOnClickListener(this);
		this.cancelButton = (Button) findViewById(R.id.cancelButton);
		this.cancelButton.setOnClickListener(this);

		this.timePicker = ((TimePicker) findViewById(R.id.digitalClockDisplay));
		this.dateTimePicker = ((DateTimePicker) findViewById(R.id.dateTimePicker));
		this.dateTimePicker.setIs24HourView(this.is24HourView);
		this.snoozeEditText = ((EditText) findViewById(R.id.snoozeTimeEditText));
		this.vibrateCheckBox = ((CheckBox) findViewById(R.id.vibrateCheckBox));
		this.nameEditText = ((EditText) findViewById(R.id.nameEditText));

		this.closestAlarm = AlarmScheduler.loadFromFile(this, this.alarmClocks);
		Intent intent = getIntent();
		int index = intent.getIntExtra(MainActivity.INDEX_ALARM, -1);
		if (index == -1) {
			this.alarmClock = new AlarmClock("Alarm "
					+ (this.alarmClocks.size() + 1), new Date(), intent
					.getBooleanExtra(MainActivity.IS_ONE_TIME_ALARM, false));
			this.alarmClocks.add(this.alarmClock);
		} else {
			boolean isClone = intent.getBooleanExtra(MainActivity.IS_CLONE,
					false);
			if (isClone) {
				this.alarmClock = new AlarmClock(this.alarmClocks.get(index));
				this.alarmClock.setName(this.alarmClock.getName() + " Clone");
				this.alarmClocks.add(this.alarmClock);
			} else {
				this.alarmClock = this.alarmClocks.get(index);
			}
		}
		updateAlarmConfigurationView();

		activateViews(this.enableCheckBox.isChecked());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (data != null) {
			this.currentMusicPaths = (TreeSet<String>) data.getExtras().get(
					AlarmConfigurationActivity.EXTRA_MUSICS);
			updateCurrentMusicTitles();
		}
	}

	/**
	 * Updates the current selected alarm with values selected by the user.
	 */
	private void updateAlarmClock() {

		// update name
		EditText nameEditText = ((EditText) findViewById(R.id.nameEditText));
		this.alarmClock.setName(nameEditText.getText().toString());

		// update enability
		CheckBox enableCheckBox = ((CheckBox) findViewById(R.id.enableCheckBox));
		this.alarmClock.setEnabled(enableCheckBox.isChecked());

		// update time
		if (this.alarmClock.isOneTimeAlarm()) {
			this.alarmClock.setDate(new Date(this.dateTimePicker
					.getDateTimeMillis()));
		} else {
			Calendar calendar = Calendar.getInstance();
			calendar.set(0, 0, 0, this.timePicker.getCurrentHour(),
					this.timePicker.getCurrentMinute());
			this.alarmClock.setDate(calendar.getTime());
		}

		// update fading
		CheckBox fadingCheckBox = ((CheckBox) findViewById(R.id.fadingCheckBox));
		EditText fadingEditText = ((EditText) findViewById(R.id.fadingEditText));
		this.alarmClock.setFading(fadingCheckBox.isChecked() ? Integer
				.parseInt(fadingEditText.getText().toString()) : 0);

		// update snooze
		this.alarmClock.setSnooze(Integer.parseInt(this.snoozeEditText
				.getText().toString()));

		// update vibration
		this.alarmClock.setVibrate(this.vibrateCheckBox.isChecked());

		// update days
		updateAlarmClockDaysFromView(R.id.mondayToggle, this.alarmClock,
				Calendar.MONDAY);
		updateAlarmClockDaysFromView(R.id.tuesdayToggle, this.alarmClock,
				Calendar.TUESDAY);
		updateAlarmClockDaysFromView(R.id.wednesdayToggle, this.alarmClock,
				Calendar.WEDNESDAY);
		updateAlarmClockDaysFromView(R.id.thursdayToggle, this.alarmClock,
				Calendar.THURSDAY);
		updateAlarmClockDaysFromView(R.id.fridayToggle, this.alarmClock,
				Calendar.FRIDAY);
		updateAlarmClockDaysFromView(R.id.saturdayToggle, this.alarmClock,
				Calendar.SATURDAY);
		updateAlarmClockDaysFromView(R.id.sundayToggle, this.alarmClock,
				Calendar.SUNDAY);

		// update musics
		this.alarmClock.setMusics(this.currentMusicPaths);
	}

	private void updateAlarmClockDaysFromView(int viewId,
			AlarmClock alarmClock, int day) {
		ToggleButton toggleButton = ((ToggleButton) findViewById(viewId));
		alarmClock.setDayActive(day, toggleButton.isChecked());
	}

	private void updateCurrentMusicTitles() {

		// Retrieve the name of the selected musics
		synchronized (this.currentSongs) {
			this.currentSongs.clear();
			for (String music : this.currentMusicPaths) {
				Cursor cursor = managedQuery(Uri.parse(music), null, null,
						null, AudioColumns.ARTIST + " ASC");
				if ((cursor != null) && cursor.moveToFirst()) {
					String title = cursor.getString(cursor
							.getColumnIndex(MediaColumns.TITLE));
					String artist = cursor.getString(cursor
							.getColumnIndex(AudioColumns.ARTIST));
					String album = cursor.getString(cursor
							.getColumnIndex(AudioColumns.ALBUM));
					this.currentSongs.add(new Song(album, artist, title));
				}
			}
			if (this.currentSongs.isEmpty()) {
				this.textSwitcher.setText(
						AlarmConfigurationActivity.DEFAULT_SONG
								.getformattedDescription(),
						TextView.BufferType.SPANNABLE);
				configureSpannable((Spannable) this.textSwitcher.getText(),
						AlarmConfigurationActivity.DEFAULT_SONG);
			} else {
				for (String music : this.currentMusicPaths) {
					Cursor cursor = managedQuery(Uri.parse(music), null, null,
							null, AudioColumns.ARTIST + " ASC");
					if ((cursor != null) && cursor.moveToFirst()) {
						String title = cursor.getString(cursor
								.getColumnIndex(MediaColumns.TITLE));
						String artist = cursor.getString(cursor
								.getColumnIndex(AudioColumns.ARTIST));
						String album = cursor.getString(cursor
								.getColumnIndex(AudioColumns.ALBUM));
						this.currentSongs.add(new Song(album, artist, title));
					}
				}
				Song song = this.currentSongs.get(0);
				this.textSwitcher.setText(song.getformattedDescription(),
						TextView.BufferType.SPANNABLE);
				configureSpannable((Spannable) this.textSwitcher.getText(),
						song);
			}
			this.textSwitcher.invalidate();
		}
	}

	/**
	 * Updates the configuration view with information of the selected alarm.
	 */
	private void updateAlarmConfigurationView() {

		// update the current list of musics
		this.currentMusicPaths.clear();
		this.currentMusicPaths.addAll(this.alarmClock.getMusics());

		// update the name
		EditText nameEditText = ((EditText) findViewById(R.id.nameEditText));
		nameEditText.setText(this.alarmClock.getName());

		// update the enable checkbox
		CheckBox enableCheckBox = ((CheckBox) findViewById(R.id.enableCheckBox));
		enableCheckBox.setChecked(this.alarmClock.isEnabled());

		// update the time picker
		TimePicker timePicker = ((TimePicker) findViewById(R.id.digitalClockDisplay));
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(this.alarmClock.getDate());
		timePicker.setIs24HourView(this.is24HourView);
		timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
		timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));

		if (this.alarmClock.isOneTimeAlarm()) {
			this.dateTimePicker.updateDate(calendar.get(Calendar.YEAR),
					calendar.get(Calendar.MONTH), calendar
							.get(Calendar.DAY_OF_MONTH));
			this.dateTimePicker.updateTime(calendar.get(Calendar.HOUR_OF_DAY),
					calendar.get(Calendar.MINUTE));
		}
		// update the fading components
		CheckBox fadingCheckBox = ((CheckBox) findViewById(R.id.fadingCheckBox));
		EditText fadingEditText = ((EditText) findViewById(R.id.fadingEditText));
		int fading = this.alarmClock.getFading();
		fadingCheckBox.setChecked(fading != 0);
		if (fading == 0) {
			fading = 30;
		}
		fadingEditText.setText(Integer.toString(fading));

		EditText snoozeEditText = ((EditText) findViewById(R.id.snoozeTimeEditText));
		snoozeEditText.setText(Integer.toString(this.alarmClock.getSnooze()));

		// update the vibrate checkbox
		CheckBox vibrateCheckBox = ((CheckBox) findViewById(R.id.vibrateCheckBox));
		vibrateCheckBox.setChecked(this.alarmClock.isVibrate());

		// update the days button
		updateDaysImageViews(R.id.mondayToggle, this.alarmClock,
				Calendar.MONDAY);
		updateDaysImageViews(R.id.tuesdayToggle, this.alarmClock,
				Calendar.TUESDAY);
		updateDaysImageViews(R.id.wednesdayToggle, this.alarmClock,
				Calendar.WEDNESDAY);
		updateDaysImageViews(R.id.thursdayToggle, this.alarmClock,
				Calendar.THURSDAY);
		updateDaysImageViews(R.id.fridayToggle, this.alarmClock,
				Calendar.FRIDAY);
		updateDaysImageViews(R.id.saturdayToggle, this.alarmClock,
				Calendar.SATURDAY);
		updateDaysImageViews(R.id.sundayToggle, this.alarmClock,
				Calendar.SUNDAY);

		updateCurrentMusicTitles();

		// View pickerLayout = findViewById(R.id.pickerLayout);
		// pickerLayout.setVisibility(View.GONE);

		View dayTableLayout = findViewById(R.id.dayTableLayout);
		dayTableLayout
				.setVisibility(this.alarmClock.isOneTimeAlarm() ? View.GONE
						: View.VISIBLE);
		timePicker.setVisibility(this.alarmClock.isOneTimeAlarm() ? View.GONE
				: View.VISIBLE);
		this.dateTimePicker
				.setVisibility(this.alarmClock.isOneTimeAlarm() ? View.VISIBLE
						: View.GONE);
	}

	private void updateDaysImageViews(int viewId, AlarmClock alarmClock, int day) {
		ToggleButton toggleButton = ((ToggleButton) findViewById(viewId));
		toggleButton.setChecked(alarmClock.isDayActive(day));
	}

	private void configureSpannable(Spannable spannable, Song song) {
		int spanIndex = 0;
		spannable.setSpan(new ForegroundColorSpan(getResources().getColor(
				R.color.dark_green)), spanIndex, song.artist.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		spannable.setSpan(new StyleSpan(Typeface.ITALIC), spanIndex,
				song.artist.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		spannable.setSpan(new AbsoluteSizeSpan(12), spanIndex, song.artist
				.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		spanIndex += 1 + song.artist.length();
		spannable.setSpan(new ForegroundColorSpan(getResources().getColor(
				R.color.dark_blue)), spanIndex,
				spanIndex + song.album.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		spannable.setSpan(new StyleSpan(Typeface.ITALIC), spanIndex, spanIndex
				+ song.album.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		spannable.setSpan(new AbsoluteSizeSpan(12), spanIndex, spanIndex
				+ song.album.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		spanIndex += 1 + song.album.length();
		spannable.setSpan(new ForegroundColorSpan(getResources().getColor(
				R.color.orange)), spanIndex, spanIndex + song.title.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		spannable.setSpan(new AbsoluteSizeSpan(15), spanIndex, spanIndex
				+ song.title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	}

	@Override
	public void onClick(View v) {

		if (v == this.saveButton) {
			updateAlarmClock();
			AlarmScheduler.saveToFile(this, this.alarmClocks);
			AlarmScheduler.scheduleAlarm(this, true, this.closestAlarm);
			finish();
		} else if (v == this.enableCheckBox) {
			activateViews(this.enableCheckBox.isChecked());
		} else {
			finish();
		}
	}

	private void activateViews(boolean activate) {
		this.nameEditText.setEnabled(activate);
		this.dateTimePicker.setEnabled(activate);
		this.timePicker.setEnabled(activate);
		this.fadingCheckBox.setEnabled(activate);
		this.fadingEditText.setEnabled(activate);
		this.vibrateCheckBox.setEnabled(activate);
		this.snoozeEditText.setEnabled(activate);
		this.textSwitcher.setEnabled(activate);
		this.browseButton.setEnabled(activate);
		findViewById(R.id.mondayToggle).setEnabled(activate);
		findViewById(R.id.tuesdayToggle).setEnabled(activate);
		findViewById(R.id.wednesdayToggle).setEnabled(activate);
		findViewById(R.id.thursdayToggle).setEnabled(activate);
		findViewById(R.id.fridayToggle).setEnabled(activate);
		findViewById(R.id.saturdayToggle).setEnabled(activate);
		findViewById(R.id.sundayToggle).setEnabled(activate);
	}
}
