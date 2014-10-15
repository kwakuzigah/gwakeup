package com.asigbe.gwakeup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class represents an alarm configured by the user.
 * 
 * @author Delali Zigah
 */
public final class AlarmClock {

	/**
	 * Convenient list used to store the list of days.
	 */
	public final static List<Integer> DAYS = new ArrayList<Integer>();
	static {
		DAYS.add(Calendar.MONDAY);
		DAYS.add(Calendar.TUESDAY);
		DAYS.add(Calendar.WEDNESDAY);
		DAYS.add(Calendar.THURSDAY);
		DAYS.add(Calendar.FRIDAY);
		DAYS.add(Calendar.SATURDAY);
		DAYS.add(Calendar.SUNDAY);
	}

	private Date date;
	private Map<Integer, Boolean> daysActive;
	private TreeSet<String> musics;
	private int fading;
	private int snooze;
	private boolean vibrate;
	private boolean enabled;
	private String name;
	private boolean oneTimeAlarm;

	/**
	 * Creates a new alarm configured with the time (hours and minutes) of the
	 * date.
	 * 
	 * @throws NullPointerException
	 *             when the given date is nulls
	 */
	public AlarmClock(String name, Date date, boolean oneTimeAlarm)
			throws NullPointerException {
		if (date == null) {
			throw new NullPointerException("date null");
		}

		this.date = date;
		this.name = name;
		this.daysActive = new HashMap<Integer, Boolean>();
		this.daysActive.put(Calendar.MONDAY, false);
		this.daysActive.put(Calendar.TUESDAY, false);
		this.daysActive.put(Calendar.WEDNESDAY, false);
		this.daysActive.put(Calendar.THURSDAY, false);
		this.daysActive.put(Calendar.FRIDAY, false);
		this.daysActive.put(Calendar.SATURDAY, false);
		this.daysActive.put(Calendar.SUNDAY, false);
		this.fading = 30;
		this.snooze = 10;
		this.vibrate = false;
		this.musics = new TreeSet<String>();
		this.enabled = true;
		this.oneTimeAlarm = oneTimeAlarm;
	}

	/**
	 * Creates a new active alarm identical to the given one.
	 */
	public AlarmClock(AlarmClock alarmClock) {
		this.name = alarmClock.name;
		this.date = alarmClock.date;
		this.daysActive = new HashMap<Integer, Boolean>(alarmClock.daysActive);
		this.musics = new TreeSet<String>(alarmClock.musics);
		this.fading = alarmClock.fading;
		this.vibrate = alarmClock.vibrate;
		this.enabled = alarmClock.enabled;
		this.oneTimeAlarm = alarmClock.oneTimeAlarm;
	}

	/**
	 * Indicates if the vibration is active.
	 */
	public boolean isVibrate() {
		return this.vibrate;
	}

	/**
	 * Sets the state of the vibration.
	 */
	public void setVibrate(boolean vibrate) {
		this.vibrate = vibrate;
	}

	/**
	 * Gets the duration of the fading in seconds.
	 */
	public int getFading() {
		return this.fading;
	}

	/**
	 * Sets the duration of the fading in seconds.
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * Gets the date of the alarm.
	 */
	public Date getDate() {
		return this.date;
	}

	/**
	 * Indicates if the alarm is active for the given day.
	 */
	public boolean isDayActive(int day) {
		return this.daysActive.get(day);
	}

	/**
	 * Sets the alarm active for the given day.
	 */
	public void setDayActive(int day, boolean active) {
		this.daysActive.put(day, active);
	}

	/**
	 * Sets the list of music which will be played by the alarm.
	 */
	public void setMusics(TreeSet<String> musics) {
		this.musics.clear();
		this.musics.addAll(musics);
	}

	/**
	 * Gets the list of music which will be played by the alarm.
	 */
	public TreeSet<String> getMusics() {
		return new TreeSet<String>(this.musics);
	}

	/**
	 * Sets the duration of the fading.
	 */
	public void setFading(int fading) {
		this.fading = fading;
	}

	/**
	 * Indicates if the alarm is enabled.
	 */
	public boolean isEnabled() {
		return this.enabled;
	}

	/**
	 * Sets the alarm enabled.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Gets the duration of the snooze in minutes.
	 */
	public int getSnooze() {
		return this.snooze;
	}

	/**
	 * Sets the duration of the snooze in minutes.
	 */
	public void setSnooze(int snooze) {
		this.snooze = snooze;
	}

	/**
	 * Gets the name of the alarm.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the name of the alarm.
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets if the alarm must be triggered once.
	 */
	public boolean isOneTimeAlarm() {
		return this.oneTimeAlarm;
	}

}