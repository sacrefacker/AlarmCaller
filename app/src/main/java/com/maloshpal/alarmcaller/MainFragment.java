package com.maloshpal.alarmcaller;

import android.Manifest;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.avast.android.dialogs.fragment.DatePickerDialogFragment;
import com.avast.android.dialogs.fragment.TimePickerDialogFragment;
import com.avast.android.dialogs.iface.IDateDialogListener;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import java.util.Date;
import java.util.TimeZone;

@EFragment(R.layout.fragment_main)
public class MainFragment extends Fragment implements IDateDialogListener
{
// MARK: - Public methods

    @Override
    public void onStart() {
        super.onStart();

        mChoseTimeButton.setText(getDefaultChoseTimeButtonText());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.allowed_phone_numbers,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mNumberSpinner.setAdapter(adapter);
        mNumberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
              @Override
              public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                  mCurrentPhoneNumber = (CharSequence) parent.getItemAtPosition(position);
                  String phoneNumber = mCurrentPhoneNumber.toString();
                  long alarmTime = StorageUtils.loadAlarm(getContext(), phoneNumber);
                  if (alarmTime < System.currentTimeMillis()) {
                      StorageUtils.removeAlarm(getContext(), phoneNumber);
                      mAlarmDate = null;
                      showAlarmTime(null);
                  }
                  else {
                      mAlarmDate = DateUtils.isTimeEmpty(alarmTime) ? DateUtils.EMPTY_DATE : new Date(alarmTime);
                      showAlarmTime(mAlarmDate);
                  }
              }

              @Override
              public void onNothingSelected(AdapterView<?> parent) {
                  mCurrentPhoneNumber = "";
                  mAlarmDate = null;
                  showAlarmTime(null);
              }
          });
    }

    public void showAlarmTime(@Nullable Date date) {
        mChoseTimeButton.setText(
                DateUtils.isDateEmpty(date) ?
                        getDefaultChoseTimeButtonText() :
                        getString(R.string.button_time_chosen, date));
    }

// MARK: - Actions

    @Click(R.id.button_set_alarm)
    public void onSetAlarmClick() {
        String phoneNumber = mCurrentPhoneNumber.toString();
        if (!TextUtils.isEmpty(phoneNumber)) {
            if (PermissionUtils.checkPermission(getActivity(), Manifest.permission.CALL_PHONE)) {
                StorageUtils.saveAlarm(getContext(), phoneNumber, mAlarmDate.getTime());
                AlarmUtils.setAlarm(getContext(), phoneNumber, mAlarmDate.getTime());
            }
            else {
                Toast.makeText(getContext(), R.string.message_permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getContext(), R.string.message_enter_phone_number, Toast.LENGTH_SHORT).show();
        }
    }

    @Click(R.id.button_remove_alarm)
    public void onRemoveAlarmClick() {
        Context context = getContext();
        String phoneNumber = mCurrentPhoneNumber.toString();
        StorageUtils.removeAlarm(context, phoneNumber);
        AlarmUtils.removeAlarm(context, phoneNumber);
        mAlarmDate = null;
        showAlarmTime(null);
    }

    @Click(R.id.button_chose_time)
    public void onChoseTimeClick() {
        showDateChooser();
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
                showAlarmTime(mAlarmDate);
                break;
            }
        }
    }

    @Override
    public void onNegativeButtonClicked(int requestCode, Date date) {
        switch (requestCode) {
            case ALARM_DATE_REQUEST_CODE: {
                // do nothing
                break;
            }
            case ALARM_TIME_REQUEST_CODE: {
                showDateChooser();
                break;
            }
        }
    }

// MARK: - Private methods

    private String getDefaultChoseTimeButtonText() {
        return getString(R.string.button_choose_time);
    }

    private void showDateChooser() {
        DatePickerDialogFragment.createBuilder(getContext(), getFragmentManager())
                .setPositiveButtonText(R.string.button_save_date)
                .setNegativeButtonText(R.string.button_cancel)
                .setTimeZone(TimeZone.getDefault().getID())
                .setTitle(R.string.title_choose_date_dialog)
                .setTargetFragment(MainFragment.this, ALARM_DATE_REQUEST_CODE)
                .show();
    }

    private void showTimeChooser(Date date) {
        TimePickerDialogFragment.createBuilder(getContext(), getFragmentManager())
                .setDate(date)
                .setPositiveButtonText(R.string.button_save_time)
                .setNegativeButtonText(R.string.button_choose_date)
                .setTitle(R.string.title_choose_time_dialog)
                .setTargetFragment(MainFragment.this, ALARM_TIME_REQUEST_CODE)
                .set24hour(true)
                .show();
    }

// MARK: - Constants

    private static final int ALARM_DATE_REQUEST_CODE = 21;
    private static final int ALARM_TIME_REQUEST_CODE = 22;

// MARK: - Variables

    @InstanceState
    CharSequence mCurrentPhoneNumber;

    @InstanceState
    Date mAlarmDate;

    @ViewById(R.id.selector_number)
    Spinner mNumberSpinner;

    @ViewById(R.id.button_chose_time)
    Button mChoseTimeButton;

    @ViewById(R.id.button_set_alarm)
    Button mSetAlarmButton;

    @ViewById(R.id.button_remove_alarm)
    Button mRemoveAlarmButton;
}
