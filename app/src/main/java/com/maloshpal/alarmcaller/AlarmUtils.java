package com.maloshpal.alarmcaller;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import java.util.Date;

public class AlarmUtils
{
// MARK: - Public methods

    public static void setAlarm(Context context, String phoneNumber, long time) {
        PendingIntent pendingIntent = makePendingIntent(context, phoneNumber, PendingIntent.FLAG_CANCEL_CURRENT);

        long cleanTime = DateUtils.clearSeconds(time);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getAlarmManager(context).setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cleanTime, pendingIntent);
        }
        else {
            getAlarmManager(context).setExact(AlarmManager.RTC_WAKEUP, cleanTime, pendingIntent);
        }

        String toast = context.getString(R.string.message_alarm_set, phoneNumber, new Date(time));
        Toast.makeText(context, toast, Toast.LENGTH_LONG).show();
    }

    public static void removeAlarm(Context context, String phoneNumber) {
        PendingIntent pendingIntent = makePendingIntent(context, phoneNumber, PendingIntent.FLAG_CANCEL_CURRENT);

        pendingIntent.cancel();
        getAlarmManager(context).cancel(pendingIntent);

        String toast = context.getString(R.string.message_alarm_canceled, phoneNumber);
        Toast.makeText(context, toast, Toast.LENGTH_LONG).show();
    }

// MARK: - Private methods

    private static AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    private static PendingIntent makePendingIntent(Context context, String phoneNumber, int flags) {
        String dial = "tel:" + phoneNumber;
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(dial));
        return PendingIntent.getActivity(context, CALL_PHONE_REQUEST_CODE, intent, flags);
    }

// MARK: - Constants

    private static final int CALL_PHONE_REQUEST_CODE = 123;
}
