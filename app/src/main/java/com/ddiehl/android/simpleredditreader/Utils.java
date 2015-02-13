package com.ddiehl.android.simpleredditreader;

import java.util.Date;

/**
 * Created by Damien on 1/19/2015.
 */
public class Utils {

    public static String getFormattedDateStringFromUtc(long utc) {
        return getFormattedDateString(new Date(utc*1000));
    }

    public static String getFormattedDateString(Date date) {
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
            output =  years;
            outputString = " year";
        } else if (weeks > 0) {
            output = weeks;
            outputString = " week";
        } else if (days > 0) {
            output = days;
            outputString = " day";
        } else if (hours > 0) {
            output = hours;
            outputString = " hour";
        } else if (minutes > 0) {
            output = minutes;
            outputString = " minute";
        } else {
            output = seconds;
            outputString = " second";
        }

        if (output > 1)
            outputString += "s";

        outputString += " ago";

        return output + outputString;
    }

}
