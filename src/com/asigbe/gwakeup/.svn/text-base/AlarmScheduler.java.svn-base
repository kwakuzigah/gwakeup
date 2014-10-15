package com.asigbe.gwakeup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TreeSet;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.provider.Settings;
import android.widget.Toast;

/**
 * This class manages the scheduleo a new alarm.
 * 
 * @author Delali Zigah
 */
public class AlarmScheduler {

	private static final String ALARM_FILE = "alarms.dat";
	
	/**
	 * Finds the closest alarm and schedules the alarm.
	 * @param displayNotification 
	 * @param closestDate 
	 */
	public static void scheduleAlarm(Context context, boolean displayNotification, Date closestDate) {

		String scheduleAlarm;
		int value = Settings.System.getInt(context.getContentResolver(),
				Settings.System.TIME_12_24, 24);
		boolean is24HourView = (value == 24);
		List<AlarmClock> alarmClocks = new ArrayList<AlarmClock>();
		loadFromFile(context, alarmClocks);

		// prepare the alarm
		Intent intent = new Intent(context, AlarmNotifyActivity.class)
				.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Activity.ALARM_SERVICE);

		// find the closest alarm to schedule
		RealAlarm closestAlarm = getClosestAlarm(alarmClocks);
		AlarmClock alarmClock = closestAlarm.getAlarmClock();
		if (alarmClock == null) {
			// cancel previously programmed alarms
			PendingIntent sender = PendingIntent.getActivity(context, 0,
					intent, PendingIntent.FLAG_CANCEL_CURRENT);
			alarmManager.cancel(sender);
			scheduleAlarm = new String(context
					.getString(R.string.noalarmscheduled));
		} else {
			// fill the intent
			String[] musics = new String[alarmClock.getMusics().size()];
			alarmClock.getMusics().toArray(musics);
			intent.putExtra("name", alarmClock.getName());
			intent.putExtra("musics", musics);
			intent.putExtra("fading", alarmClock.getFading());
			intent.putExtra("snooze", alarmClock.getSnooze());
			intent.putExtra("vibrate", alarmClock.isVibrate());

			// schedule the alarm
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			PendingIntent sender = PendingIntent.getActivity(context, 0,
					intent, PendingIntent.FLAG_CANCEL_CURRENT);
			alarmManager.set(AlarmManager.RTC_WAKEUP, closestAlarm.getDate()
					.getTime(), sender);

			// prepare the string for the status bar
			SimpleDateFormat simpleDateFormat = (SimpleDateFormat) SimpleDateFormat
					.getInstance();
			if (is24HourView) {
				simpleDateFormat.applyPattern("EEE d MMM, HH:mm");
			} else {
				simpleDateFormat.applyPattern("EEE, d MMM, KK:mm a");
			}
			scheduleAlarm = new String(context.getString(R.string.nextalarm)
					+ " " + simpleDateFormat.format(closestAlarm.getDate())
					+ ", " + alarmClock.getName());
		}

		//display the notification
		if (displayNotification) {
			Date oldClosestDate = (closestAlarm != null) ? closestAlarm.getDate() : null;
			if (closestDate  == null || !closestDate.equals(oldClosestDate)) {
				Toast.makeText(context, scheduleAlarm, Toast.LENGTH_LONG).show();
			}
		}
		/*
		// send the notification to the status bar
		if (isNotificationOn) {
			PendingIntent contentIntent = PendingIntent.getActivity(activity,
					0, new Intent(activity, MainActivity.class), 0);
			Notification notification = new Notification(
					R.drawable.gwakeup_logo_32, scheduleAlarm, System
							.currentTimeMillis());
			notification.setLatestEventInfo(activity, "GWakeUp", scheduleAlarm,
					contentIntent);
			if (isSoundNotificationOn) {
				notification.defaults = Notification.DEFAULT_SOUND;
			}
			NotificationManager notificationManager = (NotificationManager) activity
					.getSystemService(NOTIFICATION_SERVICE);
			notificationManager.notify(0, notification);
		}
		*/
	}
	

	/**
	 * Loads into memory the characteristics of alarms from a file.
	 */
	public static Date loadFromFile(Context context,
			List<AlarmClock> alarmClocks) {
		alarmClocks.clear();
		// loop through each file of the repository
		String[] fileList = context.fileList();
		for (int i = 0; i < fileList.length; i++) {
			// we only read the configuration file
			if (fileList[i].equals(ALARM_FILE)) {
				FileInputStream fileInputStream;
				try {
					// open the file
					fileInputStream = context.openFileInput(ALARM_FILE);
					BufferedReader bufferedReader = new BufferedReader(
							new InputStreamReader(fileInputStream));
					try {
						String readLine;
						int version = 0;
						SimpleDateFormat dateFormat = (SimpleDateFormat) SimpleDateFormat
								.getTimeInstance();
						dateFormat.applyPattern("HH:mm");

						while ((readLine = bufferedReader.readLine()) != null) {
							String[] splittedLine = readLine.split(";");
							if (splittedLine.length == 2
									&& splittedLine[0].equals("version")) {
								version = Integer.parseInt(splittedLine[1]);
								if (version >= 15) {
									dateFormat.applyPattern("yyyy.MM.dd HH:mm");
								}
							} else {
								if (splittedLine.length >= 10) {
									try {
										int index = 0;
										boolean oneTimeAlarm = false;
										if (version >= 15) {
											oneTimeAlarm = Boolean
													.parseBoolean(splittedLine[index++]);
										}
										AlarmClock alarmClock = new AlarmClock(
												context
														.getString(R.string.noname),
												dateFormat
														.parse(splittedLine[index++]),
												oneTimeAlarm);
										if (version > 1) {
											alarmClock
													.setName(splittedLine[index++]);
										}
										int begin = index;
										int end = index + 7;
										for (int j = begin; j < end; j++, index++) {
											alarmClock
													.setDayActive(
															AlarmClock.DAYS
																	.get(j
																			- begin),
															Boolean
																	.valueOf(splittedLine[j]));
										}

										alarmClock
												.setEnabled(Boolean
														.parseBoolean(splittedLine[index++]));
										try {
											alarmClock
													.setFading(Integer
															.parseInt(splittedLine[index++]));
										} catch (NumberFormatException e) {
										}

										alarmClock
												.setVibrate(Boolean
														.parseBoolean(splittedLine[index++]));
										alarmClock
												.setSnooze(Integer
														.parseInt(splittedLine[index++]));
										TreeSet<String> musics = new TreeSet<String>();
										for (int j = index; j < splittedLine.length; j++, index++) {
											musics.add(splittedLine[j]);
										}
										alarmClock.setMusics(musics);
										alarmClocks.add(alarmClock);
									} catch (NullPointerException e) {
									} catch (ParseException e) {
									}
								}
							}
						}
						fileInputStream.close();
					} catch (IOException e) {
					}

				} catch (FileNotFoundException e) {
				} catch (Exception e) {

				}
			}
		}
		RealAlarm closestAlarm = getClosestAlarm(alarmClocks);
		Date date = null;
		if (closestAlarm != null) {
			date = closestAlarm.getDate();
		}
		return date;
	}

	

	/**
	 * Save all alarms into file in order to restore them on the next launch.
	 */
	public static void saveToFile(Activity activity,
			List<AlarmClock> alarmClocks) {
		SimpleDateFormat dateFormat = (SimpleDateFormat) SimpleDateFormat
				.getTimeInstance();
		dateFormat.applyPattern("yyyy.MM.dd HH:mm");

		try {
			// Open the file
			FileOutputStream fileOutputStream = activity.openFileOutput(
					ALARM_FILE, Activity.MODE_PRIVATE);
			BufferedWriter bufferedWriter = new BufferedWriter(
					new OutputStreamWriter(fileOutputStream));

			PackageManager pm = activity.getPackageManager();
			PackageInfo pi;
			try {
				pi = pm.getPackageInfo(activity.getPackageName(), 0);
				int versionCode = pi.versionCode;
				bufferedWriter.write("version;" + versionCode
						+ System.getProperty("line.separator"));
			} catch (NameNotFoundException e) {
			}

			// loop through each alarm and write his characteristics into a file
			for (AlarmClock alarmClock : alarmClocks) {
				String line = new String();
				line += alarmClock.isOneTimeAlarm() + ";";
				line += dateFormat.format(alarmClock.getDate()) + ";";
				line += alarmClock.getName() + ";";
				for (int i = 0; i < 7; i++) {
					line += Boolean.toString(alarmClock
							.isDayActive(AlarmClock.DAYS.get(i)))
							+ ";";
				}
				line += Boolean.toString(alarmClock.isEnabled()) + ";";
				line += Integer.toString(alarmClock.getFading()) + ";";
				line += Boolean.toString(alarmClock.isVibrate()) + ";";
				line += Integer.toString(alarmClock.getSnooze()) + ";";
				TreeSet<String> musics = alarmClock.getMusics();
				for (String music : musics) {
					line += music + ";";
				}
				line += System.getProperty("line.separator");
				bufferedWriter.write(line);
				bufferedWriter.flush();
			}
			fileOutputStream.close();
		} catch (FileNotFoundException e1) {
		} catch (IOException e) {
		}
	}


	private static final class RealAlarm {
		private final Date date;
		private final AlarmClock alarmClock;

		public RealAlarm(AlarmClock alarmClock, Date date) {
			this.alarmClock = alarmClock;
			this.date = date;
		}

		public Date getDate() {
			return date;
		}

		public AlarmClock getAlarmClock() {
			return alarmClock;
		}
	}

	private static RealAlarm getClosestAlarm(List<AlarmClock> alarmClocks) {
		Calendar alarmCalendar = new GregorianCalendar();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		Date currentTime = calendar.getTime();
		Date closestAlarm = null;
		AlarmClock closestAlarmClock = null;
		for (AlarmClock alarmClock : alarmClocks) {
			if (alarmClock.isEnabled()) {
				if (alarmClock.isOneTimeAlarm()) {
					if ((closestAlarm == null || closestAlarm.after(alarmClock
							.getDate()))
							&& alarmClock.getDate().after(currentTime)) {
						closestAlarm = alarmClock.getDate();
						closestAlarmClock = alarmClock;
					}
				} else {
					calendar.setTimeInMillis(System.currentTimeMillis());
					int day = AlarmClock.DAYS.indexOf(calendar
							.get(Calendar.DAY_OF_WEEK));
					int i = 0;
					do {
						alarmCalendar.setTime(alarmClock.getDate());
						calendar.setTimeInMillis(System.currentTimeMillis());
						calendar.add(Calendar.DAY_OF_MONTH, i);
						calendar.set(Calendar.HOUR_OF_DAY, alarmCalendar
								.get(Calendar.HOUR_OF_DAY));
						calendar.set(Calendar.MINUTE, alarmCalendar
								.get(Calendar.MINUTE));
						calendar.set(Calendar.SECOND, 0);
						calendar.set(Calendar.MILLISECOND, 0);
					} while ((i++ < 8 && !alarmClock
							.isDayActive(AlarmClock.DAYS.get(day++ % 7)))
							|| calendar.getTime().before(currentTime));

					if (alarmClock.isDayActive(AlarmClock.DAYS.get(--day % 7))
							&& calendar.getTime().after(currentTime)) {
						if (closestAlarm == null
								|| closestAlarm.after(calendar.getTime())) {
							closestAlarm = calendar.getTime();
							closestAlarmClock = alarmClock;
						}
					}
				}
			}
		}
		return new RealAlarm(closestAlarmClock, closestAlarm);
	}
}
