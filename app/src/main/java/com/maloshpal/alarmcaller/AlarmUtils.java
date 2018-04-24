package com.maloshpal.alarmcaller;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class AlarmUtils
{
    public static AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public static PendingIntent makePendingIntent(Context context, String phoneNumber, int flags) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(phoneNumber));
        return PendingIntent.getActivity(context, CALL_PHONE_REQUEST_CODE, intent, flags);
    }

    public static void setAlarm(Context context, long time, PendingIntent pendingIntent) {
        AlarmManager alarmManager = getAlarmManager(context);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }

    public static void removeAlarm(Context context) {
        String phoneNumber = retrievePhoneNumber(context);
        PendingIntent pendingIntent = makePendingIntent(context, phoneNumber, 0);
        pendingIntent.cancel();
        getAlarmManager(context).cancel(pendingIntent);

        String toast = context.getString(R.string.label_alarm_canceled, phoneNumber);
        Toast.makeText(context, toast, Toast.LENGTH_LONG).show();

    }

    public static void savePhoneNumber(Context context, String phoneNumber) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREFS_PHONE_NUMBER, phoneNumber);
        editor.apply();
    }

    public static String retrievePhoneNumber(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(PREFS_PHONE_NUMBER, "");
    }

    private static final int CALL_PHONE_REQUEST_CODE = 123;
    private static final String PREFS_PHONE_NUMBER = "PREFS_PHONE_NUMBER";
}
