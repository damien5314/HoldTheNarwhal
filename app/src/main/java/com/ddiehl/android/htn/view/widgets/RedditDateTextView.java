/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ddiehl.android.htn.R;

import java.util.Date;

public class RedditDateTextView extends TextView {

    private static final int[] TIMESPAN_IDS = {
            R.string.timespan_years,
            R.string.timespan_months,
            R.string.timespan_weeks,
            R.string.timespan_days,
            R.string.timespan_hours,
            R.string.timespan_minutes,
            R.string.timespan_seconds,
            R.string.timespan_now
    };

    private static final int[] TIMESPAN_IDS_PLURAL = {
            R.string.timespan_years_plural,
            R.string.timespan_months_plural,
            R.string.timespan_weeks_plural,
            R.string.timespan_days_plural,
            R.string.timespan_hours_plural,
            R.string.timespan_minutes_plural,
            R.string.timespan_seconds_plural,
            R.string.timespan_now
    };

    private static final int[] TIMESPAN_IDS_ABBR = {
            R.string.timespan_year_abbr,
            R.string.timespan_months_abbr,
            R.string.timespan_weeks_abbr,
            R.string.timespan_days_abbr,
            R.string.timespan_hours_abbr,
            R.string.timespan_minutes_abbr,
            R.string.timespan_seconds_abbr,
            R.string.timespan_now
    };

    private boolean mAbbreviated = false;

    public RedditDateTextView(Context context) {
        super(context);
    }

    public RedditDateTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAbbreviated = attrs.getAttributeBooleanValue("http://schemas.android.com/apk/res-auto",
                "abbreviated", false);
    }

    public void setDate(Date date) {
        setText(getFormattedDateString(date));
    }

    public void setDate(long utc) {
        setText(getFormattedDateString(new Date(utc * 1000)));
    }

    private String getFormattedDateString(Date date) {
        long currentTime = System.currentTimeMillis();
        long differential = currentTime - date.getTime();

        long seconds = differential / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        long months = days / 30;
        long years = days / 365;

        long[] units = new long[] { years, months, weeks, days, hours, minutes, seconds };

        String output = "";
        long unit = 0;
        for (int i = 0; i < units.length; i++) {
            unit = units[i];
            if (unit > 0) {
                if (mAbbreviated) {
                    output = getContext().getString(TIMESPAN_IDS_ABBR[i]);
                } else if (unit == 1) {
                    output = getContext().getString(TIMESPAN_IDS[i]);
                } else {
                    output = getContext().getString(TIMESPAN_IDS_PLURAL[i]);
                }
                break;
            }
        }

        // If < 10 seconds ago, use "now"
        if (unit == seconds && seconds <= 10) {
            output = getContext().getString(TIMESPAN_IDS[TIMESPAN_IDS.length - 1]);
        }

        return String.format(output, unit);
//        return DateUtils.getRelativeTimeSpanString(date.getTime(), new Date().getTime(), DateUtils.MINUTE_IN_MILLIS).toString();
    }

    public void setEdited(boolean edited) {
        setText(edited ? getText() + "*" : getText().toString().replace("*", ""));
    }
}
