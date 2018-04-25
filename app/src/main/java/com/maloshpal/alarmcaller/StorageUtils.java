package com.maloshpal.alarmcaller;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class StorageUtils
{
    public static void saveAlarm(Context context, String phoneNumber, long alarmTime) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(makeKey(phoneNumber), alarmTime);
        editor.apply();
    }

    public static long loadAlarm(Context context, String phoneNumber) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getLong(makeKey(phoneNumber), DateUtils.EMPTY_TIME);
    }

    public static void removeAlarm(Context context, String phoneNumber) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(makeKey(phoneNumber));
        editor.apply();
    }

    private static String makeKey(String phoneNumber) {
        return PREFS_PHONE_NUMBER + ":" + phoneNumber;
    }

    private static final String PREFS_PHONE_NUMBER = "PREFS_PHONE_NUMBER";
}
