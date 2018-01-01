package com.hitake.www.momclock;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by Oded & Yael on 26-Jun-16.
 */
public class CalendarReader  {

    Handler mHandler = null;
    Activity mActivity = null;
    boolean mSwitchedActivity = false;
    private final int TIME_INTERVAL = 1000;
    private final int INITIAL_INTERVAL = 1;
    private final boolean SHOW_SECONDS = false;
    private final boolean BLINK_COLONS = false;
    private final int REFRESH_BATTERY_DELAY = 30; // seconds
    private final int REFRESH_ACTIVITY_DELAY = 10; // minutes
    private BatteryReceiver mBattery = null;

    public CalendarReader(Activity aActivity) {
        mActivity = aActivity;
    }

    private void StartClock() {
        /*if (mBattery == null) {
            mBattery = new BatteryReceiver(this);
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
            iFilter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
            iFilter.addAction("android.intent.action.ACTION_BATTERY_CHANGED");
            mActivity.registerReceiver(mBattery, iFilter);
        }*/

        if (mHandler != null) {
            doClock();
            mHandler.postDelayed(myClock, TIME_INTERVAL);
        }
        else {
            mHandler = new Handler();
            mHandler.postDelayed(myClock, INITIAL_INTERVAL);
        }
    }

    public void SetActivity(Activity aActivity) {
        mActivity = aActivity;
        updateBatteryView();
        StartClock();
    }

    private final Runnable myClock = new Runnable() {
        @Override
        public void run() {

            boolean switchCondition = doClock();
            if (switchCondition && !mSwitchedActivity) {

                Intent myIntent = new Intent(mActivity.getBaseContext(), getActivityClass());
                mSwitchedActivity = true;
                mHandler.removeCallbacks(this);
                mActivity.startActivity(myIntent);
            }
            else
                mHandler.postDelayed(this, TIME_INTERVAL);

            if (!switchCondition)
                mSwitchedActivity = false;

            //mHandler.postDelayed(this, TIME_INTERVAL);
        }
    };

    private boolean doClock() {
        Calendar c = Calendar.getInstance();
        int seconds = c.get(Calendar.SECOND);
        int minutes = c.get(Calendar.MINUTE);
        int hours = c.get(Calendar.HOUR);
        int ampm = c.get(Calendar.AM_PM);  // 0,1
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH); // +1
        updateTimeView(hours, minutes,seconds, ampm);
        updateDateView(dayOfWeek, day, month + 1);
        updatePeriodView(hours, minutes, ampm);
        if (REFRESH_BATTERY_DELAY != 0 && seconds % REFRESH_BATTERY_DELAY ==0)
            updateBatteryView();
        return (minutes % REFRESH_ACTIVITY_DELAY) == 0;
    }

    private Class getActivityClass() {
        if (mActivity instanceof MainActivity)
                return AltActivity.class;
        else
                return MainActivity.class;
    }

    private void updateTimeView(int Hours, int Minutes, int Seconds, int AmPm) {
        String currTime;
        if (Hours == 0)
            Hours = 12;
        if (Hours < 10)
            currTime = "0" + Hours;
        else
            currTime = ""+Hours;
        if (BLINK_COLONS) {
            if (Seconds %2 == 0)
                currTime += ":";
            else
                currTime += " ";
        }
        else
            currTime += ":";
        if (Minutes < 10)
            currTime += "0" + Minutes;
        else
            currTime += Minutes;
        if (SHOW_SECONDS) {
            if (Seconds < 10)
                currTime += ":0" + Seconds;
            else
                currTime += ":"+Seconds;
        }
        TextView textView = (TextView) mActivity.findViewById(R.id.timeView);
        if (textView != null)
            textView.setText(currTime);
        return;
    }

    private void updatePeriodView(int Hours, int Minutes, int AmPm) {
        String str;
        if (AmPm == 0) { // morning
            if (Hours < 3) str = getRString(R.string.night);
            else if (Hours < 6) str = getRString(R.string.early);
            else str = getRString(R.string.morning);
        } else { // after noon
            if (Hours < 3) str = getRString(R.string.noon);
            else if (Hours < 5) str = getRString(R.string.after_noon);
            else if (Hours < 8) str = getRString(R.string.evening);
            else str = getRString(R.string.night);
        }
        TextView textView = (TextView) mActivity.findViewById(R.id.periodView);
        if (textView != null)
            textView.setText(str);
        return;
    }

    private void updateDateView(int DayOfWeek, int Day, int Month) {
        String str;
        str = getRString(R.string.today_is) +" ";
        str += translateDayOfWeek(DayOfWeek) + " " +getRString(R.string.is_the);
        str += translateDate(Day, Month);
        TextView textView = (TextView) mActivity.findViewById(R.id.dateView);
        if (textView != null)
            textView.setText(str);
        return;
    }

    protected void updateBatteryView() {
        if (mActivity == null)
            return;
        boolean isCharging = isPhonePluggedIn();
        String str = getBatteryLevel()+"%";
        if (isCharging)
                str += " +";
        TextView textView = (TextView) mActivity.findViewById(R.id.batteryView);
        if (textView != null)
            textView.setText(str);
        return;
    }

    private String translateDayOfWeek(int DayOfWeek) {
        switch (DayOfWeek) {
            case 1:
                return getRString(R.string.sunday);
            case 2:
                return getRString(R.string.monday);
            case 3:
                return getRString(R.string.tuesday);
            case 4:
                return getRString(R.string.wednesday);
            case 5:
                return getRString(R.string.thursday);
            case 6:
                return getRString(R.string.friday);
            case 7:
                return getRString(R.string.saturday);
            default:
                return "ERR";
        }
    }

    private String translateDate(int Day, int Month) {
        String theDate = Day + " " + getRString(R.string.in);
        switch (Month) {
            case 1:
                return theDate + getRString(R.string.jan);
            case 2:
                return theDate + getRString(R.string.feb);
            case 3:
                return theDate + getRString(R.string.mar);
            case 4:
                return theDate + getRString(R.string.apr);
            case 5:
                return theDate + getRString(R.string.may);
            case 6:
                return theDate + getRString(R.string.jun);
            case 7:
                return theDate + getRString(R.string.jul);
            case 8:
                return theDate + getRString(R.string.aug);
            case 9:
                return theDate + getRString(R.string.sep);
            case 10:
                return theDate + getRString(R.string.oct);
            case 11:
                return theDate + getRString(R.string.nov);
            case 12:
                return theDate + getRString(R.string.dec);
            default:
                return "ERR";
        }

    }

    public int getBatteryLevel() {
        Intent batteryIntent = mActivity.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            return 50;
        }

        float lev = ((float)level / (float)scale) * 100.0f;
        return Math.round(lev);
    }

    public boolean isPhonePluggedIn() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = mActivity.registerReceiver(null, ifilter);
        // Are we charging / charged?
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        // How are we charging?
        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        return isCharging;
    }

    private String getRString(int id) {
        return mActivity.getBaseContext().getResources().getString(id);
    }

    private void msg(String text) {
        Toast.makeText(mActivity.getBaseContext(),text,Toast.LENGTH_SHORT).show();
    }

    protected void destroy() {
        //mActivity.unregisterReceiver(mBattery);
    }

}
