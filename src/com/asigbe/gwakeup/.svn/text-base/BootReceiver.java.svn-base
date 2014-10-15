package com.asigbe.gwakeup;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This receiver is used to set the alarm on startup of the phone.
 * 
 * @author Delali Zigah
 */
public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		AlarmScheduler.scheduleAlarm(context, false, new Date());
	}

}
