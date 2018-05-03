package com.maloshpal.alarmcaller;

import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.Date;

public class DateUtils
{
    public static long clearSeconds(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(time));
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime().getTime();
    }

    public static boolean isTimeEmpty(long time) {
        return time < 0;
    }

    public static boolean isDateEmpty(@Nullable Date date) {
        return date == null || !date.after(EMPTY_DATE);
    }

    public static final long EMPTY_TIME = -1;

    public static final Date EMPTY_DATE = makeEmptyDate();

    private static Date makeEmptyDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(1, 1, 1);
        return cal.getTime();
    }
}
