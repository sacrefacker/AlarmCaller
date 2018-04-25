package com.maloshpal.alarmcaller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.telephony.SmsMessage;
import android.text.TextUtils;

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

        String[] phoneNumbers = context.getResources().getStringArray(R.array.allowed_phone_numbers);
        Pattern commandPattern = Pattern.compile(context.getString(R.string.sms_command_regex));
        Pair<String, String> numberAndMessage = findSmsForPhoneNumbers(intent.getExtras(), phoneNumbers, commandPattern);

        if (numberAndMessage != null) {
            startSettingAlarm(context, numberAndMessage.first, parseTime(numberAndMessage.second));
        }
    }

// MARK: - Private methods

    private void startSettingAlarm(Context context, String phoneNumber, long millis) {

        Intent alarmIntent = new Intent(context, SetterActivity_.class);
        Bundle options = SetterActivity.newExtras(phoneNumber, millis);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.putExtras(options);
        context.startActivity(alarmIntent);
    }

    private long parseTime(String timeString) {
        LocalTime time = LocalTime.parse(timeString);

        ZonedDateTime dateTime;
        if (LocalTime.now().isBefore(time)) {
            dateTime = ZonedDateTime.of(LocalDate.now(), time, ZoneId.systemDefault());
        }
        else {
            dateTime = ZonedDateTime.of(LocalDate.now().plus(1, ChronoUnit.DAYS), time, ZoneId.systemDefault());
        }
        return dateTime.toInstant().toEpochMilli();
    }

    private Pair<String, String> findSmsForPhoneNumbers(Bundle bundle, String[] phoneNumbers, Pattern commandPattern) {

        // Get the SMS message.
        String format = bundle.getString("format");
        Object[] pdus = (Object[]) bundle.get(PDU_TYPE);

        Pair<String, String> result = null;
        if (pdus != null) {
            SmsMessage[] messages = new SmsMessage[pdus.length];

            Matcher commandMatcher;
            for (int i = 0; i < messages.length; i++) {

                // Check Android version and use appropriate createFromPdu.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // If Android version M or newer:
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                } else {
                    // If Android version L or older:
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }

                String allowedOrigin = getPhoneFromAllowed(phoneNumbers, messages[i].getOriginatingAddress());
                if (!TextUtils.isEmpty(allowedOrigin)) {

                    String messageText = messages[i].getMessageBody();
                    commandMatcher = commandPattern.matcher(messageText);

                    if (commandMatcher.matches()) {
                        result = new Pair<>(allowedOrigin, messageText);
                        break;
                    }
                }
            }
        }

        return result;
    }

    private String getPhoneFromAllowed(String[] allowedPhones, String messageSender) {
        for (String phone: allowedPhones) {
            if (messageSender.equals(phone)) {
                return phone;
            }
        }
        return "";
    }

// MARK: - Constants

    public static final String PDU_TYPE = "pdus";
}
