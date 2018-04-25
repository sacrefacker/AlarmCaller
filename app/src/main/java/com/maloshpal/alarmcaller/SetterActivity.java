package com.maloshpal.alarmcaller;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.androidannotations.annotations.EActivity;

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

        Bundle options = getIntent().getExtras();
        String phoneNumber = options.getString(EXTRA_PHONE_NUMBER);
        long time = options.getLong(EXTRA_TIME);

        if (PermissionUtils.checkPermission(SetterActivity.this, Manifest.permission.CALL_PHONE)) {
            StorageUtils.saveAlarm(SetterActivity.this, phoneNumber, time);
            AlarmUtils.setAlarm(SetterActivity.this, phoneNumber, time);
        }
        else {
            Toast.makeText(SetterActivity.this, R.string.message_permission_denied, Toast.LENGTH_SHORT).show();
        }

        goToMainScreen();
    }

// MARK: - Private methods

    private void goToMainScreen() {
        Intent alarmIntent = new Intent(SetterActivity.this, MainActivity_.class);
        startActivity(alarmIntent);
        finish();
    }

// MARK: - Constants

    private static final String TAG = "SmsReceiver";

    public static final String EXTRA_PHONE_NUMBER = "PHONE_NUMBER";
    public static final String EXTRA_TIME = "TIME";
}
