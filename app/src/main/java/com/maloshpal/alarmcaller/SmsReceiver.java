package com.maloshpal.alarmcaller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsReceiver extends BroadcastReceiver
{
// MARK: - Public methods

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");

        String phoneNumber = context.getString(R.string.default_phone_number);
        Pattern commandPattern = Pattern.compile(context.getString(R.string.sms_command_regex));
        String messageText = findSmsForPhoneNumber(intent.getExtras(), phoneNumber, commandPattern);
        LocalTime time = LocalTime.parse(messageText);

        ZonedDateTime dateTime;
        if (LocalTime.now().isBefore(time)) {
            dateTime = ZonedDateTime.of(LocalDate.now(), time, ZoneId.systemDefault());
        }
        else {
            dateTime = ZonedDateTime.of(LocalDate.now().plus(1, ChronoUnit.DAYS), time, ZoneId.systemDefault());
        }

        Intent alarmIntent = new Intent(context, SetterActivity_.class);
        Bundle options = SetterActivity.newExtras(phoneNumber, dateTime.toInstant().toEpochMilli());
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.putExtras(options);
        context.startActivity(alarmIntent);
    }

// MARK: - Private methods

    private String findSmsForPhoneNumber(Bundle bundle, String phoneNumber, Pattern commandPattern) {
        Log.d(TAG, "inside findSmsForPhoneNumber");

        // Get the SMS message.
        String format = bundle.getString("format");
        Object[] pdus = (Object[]) bundle.get(PDU_TYPE);

        String result = "";
        if (pdus != null) {
            Log.d(TAG, "inside pdus non null");

            SmsMessage[] messages = new SmsMessage[pdus.length];

            Matcher commandMatcher;
            for (int i = 0; i < messages.length; i++) {
                Log.d(TAG, "inside loop");

                // Check Android version and use appropriate createFromPdu.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // If Android version M or newer:
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                } else {
                    // If Android version L or older:
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }

                if (messages[i].getOriginatingAddress().equals(phoneNumber)) {
                    Log.d(TAG, "inside phone number");

                    String messageText = messages[i].getMessageBody();
                    commandMatcher = commandPattern.matcher(messageText);

                    if (commandMatcher.matches()) {
                        Log.d(TAG, "inside matcher");

                        result = messages[i].getMessageBody();
                        break;
                    }
                }
            }
        }

        return result;
    }

// MARK: - Constants

    private static final String TAG = "SmsReceiver";

    public static final String PDU_TYPE = "pdus";
}
