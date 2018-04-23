package com.maloshpal.alarmcaller;

import android.Manifest;
import android.app.AlarmManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.avast.android.dialogs.fragment.DatePickerDialogFragment;
import com.avast.android.dialogs.fragment.TimePickerDialogFragment;
import com.avast.android.dialogs.iface.IDateDialogListener;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import java.sql.Timestamp;
import java.util.Date;
import java.util.TimeZone;

@EFragment(R.layout.fragment_main)
public class MainFragment extends Fragment implements IDateDialogListener
{
// MARK: - Public methods

    @Override
    public void onStart() {
        super.onStart();

        mAlarmDate = new Date();
        showPickupTime(new Timestamp(mAlarmDate.getTime()));
    }

    public void showPickupTime(@Nullable Timestamp timestamp) {
        String orderTime = timestamp == null ?
                getString(R.string.btn_booking_time_urgently) :
                getString(R.string.format_time_booking, timestamp);

        mChosenTimeText.setText(getString(R.string.label_choose_time, orderTime));
    }

// MARK: - Actions

    @Click(R.id.button_set_alarm)
    public void onSetAlarmClick() {
        String phoneNumber = mNumberText.getText().toString();

        if (!TextUtils.isEmpty(phoneNumber)) {
            if (PermissionTools.checkPermission(getActivity(), Manifest.permission.CALL_PHONE)) {
                String dial = "tel:" + phoneNumber;
                savePhoneNumber(dial);
                PendingIntent pendingIntent = makePendingIntent(dial, 0);
                setAlarm(pendingIntent);
                Toast.makeText(getContext(), getString(R.string.label_alarm_set, phoneNumber), Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getContext(), R.string.label_permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getContext(), R.string.label_enter_phone_number, Toast.LENGTH_SHORT).show();
        }
    }

    @Click(R.id.button_remove_alarm)
    public void onRemoveAlarmClick() {
        String phoneNumber = retrievePhoneNumber();
        PendingIntent pendingIntent = makePendingIntent(phoneNumber, 0);
        pendingIntent.cancel();
        getAlarmManager().cancel(pendingIntent);
        Toast.makeText(getContext(), getString(R.string.label_alarm_canceled, phoneNumber), Toast.LENGTH_LONG).show();
    }

    @Click(R.id.button_chose_time)
    public void onChoseTimeClick() {
        showDateChooser(mAlarmDate);
    }

    @Override
    public void onPositiveButtonClicked(int requestCode, Date date) {
        switch (requestCode) {
            case ALARM_DATE_REQUEST_CODE: {
                showTimeChooser(date);
                break;
            }
            case ALARM_TIME_REQUEST_CODE: {
                mAlarmDate = date;
                showPickupTime(new Timestamp(date.getTime()));
                break;
            }
        }
    }

    @Override
    public void onNegativeButtonClicked(int requestCode, Date date) {
        switch (requestCode) {
            case ALARM_DATE_REQUEST_CODE: {
                mAlarmDate = new Date();
                showPickupTime(null);
                break;
            }
            case ALARM_TIME_REQUEST_CODE: {
                showDateChooser(mAlarmDate);
                break;
            }
        }
    }

// MARK: - Private methods

    private AlarmManager getAlarmManager() {
        return (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
    }

    private PendingIntent makePendingIntent(String phoneNumber, int flags) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(phoneNumber));
        return PendingIntent.getActivity(getContext(), CALL_PHONE_REQUEST_CODE, intent, flags);
    }

    private void setAlarm(PendingIntent pendingIntent) {
        AlarmManager alarmManager = getAlarmManager();
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, mAlarmDate.getTime(), pendingIntent);
    }

    private void savePhoneNumber(String phoneNumber) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREFS_PHONE_NUMBER, phoneNumber);
        editor.apply();
    }

    private String retrievePhoneNumber() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getString(PREFS_PHONE_NUMBER, "");
    }

    private void showDateChooser(Date date) {
        DatePickerDialogFragment.createBuilder(getContext(), getFragmentManager())
                .setDate(date)
                .setPositiveButtonText(R.string.btn_save_date)
                .setNegativeButtonText(R.string.btn_booking_time_urgently)
                .setTimeZone(TimeZone.getDefault().getID())
                .setTitle(R.string.title_choose_date_dialog)
                .setTargetFragment(MainFragment.this, ALARM_DATE_REQUEST_CODE)
                .show();
    }

    private void showTimeChooser(Date date) {
        TimePickerDialogFragment.createBuilder(getContext(), getFragmentManager())
                .set24hour(true)
                .setPositiveButtonText(R.string.btn_save_time)
                .setNegativeButtonText(R.string.btn_choose_date)
                .setTitle(R.string.title_choose_time_dialog)
                .setDate(date)
                .setTargetFragment(MainFragment.this, ALARM_TIME_REQUEST_CODE)
                .show();
    }

// MARK: - Constants

    private static final int CALL_PHONE_REQUEST_CODE = 123;
    private static final String PREFS_PHONE_NUMBER = "PREFS_PHONE_NUMBER";

    private static final int ALARM_DATE_REQUEST_CODE = 21;
    private static final int ALARM_TIME_REQUEST_CODE = 22;

// MARK: - Variables

    @InstanceState
    Date mAlarmDate;

    @ViewById(R.id.edit_number)
    EditText mNumberText;

    @ViewById(R.id.label_chosen_time)
    TextView mChosenTimeText;

    @ViewById(R.id.button_chose_time)
    Button mChoseTimeButton;

    @ViewById(R.id.button_set_alarm)
    Button mSetAlarmButton;

    @ViewById(R.id.button_remove_alarm)
    Button mRemoveAlarmButton;
}
