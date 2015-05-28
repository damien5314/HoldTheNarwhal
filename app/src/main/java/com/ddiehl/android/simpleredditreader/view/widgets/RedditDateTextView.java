package com.ddiehl.android.simpleredditreader.view.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;

import java.util.Date;

public class RedditDateTextView extends TextView {

    public RedditDateTextView(Context context) {
        super(context);
    }

    public RedditDateTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
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

        long output;
        String outputString;
        if (years > 0) {
            output = years;
            outputString = getContext().getString(R.string.timespan_year);
        } else if (weeks > 0) {
            output = weeks;
            outputString = getContext().getString(R.string.timespan_weeks);
        } else if (days > 0) {
            output = days;
            outputString = getContext().getString(R.string.timespan_days);
        } else if (hours > 0) {
            output = hours;
            outputString = getContext().getString(R.string.timespan_hours);
        } else if (minutes > 0) {
            output = minutes;
            outputString = getContext().getString(R.string.timespan_minutes);
        } else {
            output = seconds;
            outputString = getContext().getString(R.string.timespan_seconds);
        }

        return String.format(outputString, output);
//        return DateUtils.getRelativeTimeSpanString(date.getTime(), new Date().getTime(), DateUtils.MINUTE_IN_MILLIS).toString();
    }

    public void setEdited(boolean edited) {
        setText(edited ? getText() + "*" : getText().toString().replace("*", ""));
    }
}
