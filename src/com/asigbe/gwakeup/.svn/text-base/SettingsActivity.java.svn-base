package com.asigbe.gwakeup;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;

import com.asigbe.preference.VolumePreference;

/**
 * This class manage the activity which allows to modify the settings.
 * 
 * @author Delali Zigah
 */
public class SettingsActivity extends PreferenceActivity {

    /** key used to save notification setting */
    //public static final String SHOW_NOTIFICATION_KEY  = "show_notification";
    /** key used to save sound notification setting */
    //public static final String SOUND_NOTIFICATION_KEY = "sound_notification";
    /** key used to save shaking setting */
    public static final String SNOOZE_SHAKE_KEY       = "snooze_shake";
    /** key used to save volume setting */
    public static final String ALARM_VOLUME_KEY       = "alarm_volume";

    private CheckBoxPreference soundNotification;
    private VolumePreference   alarmVolume;

    @Override
    protected void onCreate(Bundle icicle) {
	super.onCreate(icicle);
	addPreferencesFromResource(R.xml.settings);
	//this.soundNotification = (CheckBoxPreference) findPreference(SOUND_NOTIFICATION_KEY);
	this.alarmVolume = (VolumePreference) findPreference(ALARM_VOLUME_KEY);
    }
/*
    @Override
    protected void onResume() {
	this.soundNotification.setDependency(SHOW_NOTIFICATION_KEY);
	super.onResume();
    }
*/
    @Override
    protected void onStop() {
	this.alarmVolume.cleanup();
	super.onStop();
    }
}
