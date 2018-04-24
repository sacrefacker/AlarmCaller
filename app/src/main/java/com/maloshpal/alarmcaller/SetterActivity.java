package com.maloshpal.alarmcaller;

import android.Manifest;
import android.app.PendingIntent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.androidannotations.annotations.EActivity;

import java.util.Date;

@EActivity(R.layout.activity_alarm)
public class SetterActivity extends AppCompatActivity
{
// MARK: - Static functions

    public static Bundle newExtras(String phoneNumber, long time) {
        Bundle options = new Bundle();
        options.putString(EXTRA_PHONE_NUMBER, phoneNumber);
        options.putLong(EXTRA_TIME, time);
        return options;
    }

// MARK: - Public methods

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "inside onStart");

        Bundle options = getIntent().getExtras();
        String phoneNumber = options.getString(EXTRA_PHONE_NUMBER);
        long time = options.getLong(EXTRA_TIME);

        if (PermissionUtils.checkPermission(SetterActivity.this, Manifest.permission.CALL_PHONE)) {
            String dial = "tel:" + phoneNumber;
            AlarmUtils.savePhoneNumber(SetterActivity.this, dial);
            PendingIntent pendingIntent = AlarmUtils.makePendingIntent(SetterActivity.this, dial, 0);
            AlarmUtils.setAlarm(SetterActivity.this, time, pendingIntent);

            String toast = SetterActivity.this.getString(R.string.label_alarm_set, phoneNumber, new Date(time));
            Toast.makeText(SetterActivity.this, toast, Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(SetterActivity.this, R.string.label_permission_denied, Toast.LENGTH_SHORT).show();
        }
    }

// MARK: - Constants

    private static final String TAG = "SmsReceiver";

    public static final String EXTRA_PHONE_NUMBER = "PHONE_NUMBER";
    public static final String EXTRA_TIME = "TIME";
}
