package com.hitake.www.momclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

/**
 * Created by odedc on 29-Jun-16.
 */
public class BatteryReceiver extends BroadcastReceiver {

    private boolean mDestroy = false;
    private CalendarReader mCalendar;

    public BatteryReceiver(CalendarReader calendar) {
        mCalendar = calendar;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mDestroy) {
            context.unregisterReceiver(this);
        }
        else {
            mCalendar.updateBatteryView();
        }
    }

    public void destroy() {
        mDestroy = true;
    }
}
