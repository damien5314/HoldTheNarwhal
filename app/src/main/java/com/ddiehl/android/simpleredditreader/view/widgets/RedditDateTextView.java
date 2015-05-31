package com.ddiehl.android.simpleredditreader.view.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;

import java.util.Date;

public class RedditDateTextView extends TextView {

    private static final int[] TIMESPAN_IDS = {
            R.string.timespan_year,
            R.string.timespan_weeks,
            R.string.timespan_days,
            R.string.timespan_hours,
            R.string.timespan_minutes,
            R.string.timespan_seconds,
            R.string.timespan_now
    };

    private static final int[] TIMESPAN_IDS_ABBR = {
            R.string.timespan_year_abbr,
            R.string.timespan_weeks_abbr,
            R.string.timespan_days_abbr,
            R.string.timespan_hours_abbr,
            R.string.timespan_minutes_abbr,
            R.string.timespan_seconds_abbr,
            R.string.timespan_now_abbr
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
        long years = days / 365;

        int[] timespanIds = mAbbreviated ? TIMESPAN_IDS_ABBR : TIMESPAN_IDS;
        long output;
        String outputString;
        if (years > 0) {
            output = years;
            outputString = getContext().getString(timespanIds[0]);
        } else if (weeks > 0) {
            output = weeks;
            outputString = getContext().getString(timespanIds[1]);
        } else if (days > 0) {
            output = days;
            outputString = getContext().getString(timespanIds[2]);
        } else if (hours > 0) {
            output = hours;
            outputString = getContext().getString(timespanIds[3]);
        } else if (minutes > 0) {
            output = minutes;
            outputString = getContext().getString(timespanIds[4]);
        } else {
            output = seconds;
            outputString = getContext().getString(seconds < 10
                    ? timespanIds[6] : timespanIds[5]);
        }

        return String.format(outputString, output);
//        return DateUtils.getRelativeTimeSpanString(date.getTime(), new Date().getTime(), DateUtils.MINUTE_IN_MILLIS).toString();
    }

    public void setEdited(boolean edited) {
        setText(edited ? getText() + "*" : getText().toString().replace("*", ""));
    }
}
