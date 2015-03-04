package com.ddiehl.android.simpleredditreader;

import android.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

import retrofit.client.Header;
import retrofit.client.Response;
import retrofit.mime.TypedInput;

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

    public static void printResponse(Response response) {
        printResponseStatus(response);
        printResponseHeaders(response);
        printResponseBody(response);
    }

    public static void printResponseStatus(Response response) {
        System.out.println("URL: " + response.getUrl() + " (STATUS: " + response.getStatus() + ")");
//        System.out.println("REASON: " + response.getReason());
    }

    public static void printResponseHeaders(Response response) {
        System.out.println("--HEADERS--");
        List<Header> headersList = response.getHeaders();
        for (Header header : headersList) {
            System.out.println(header.toString());
        }
    }

    public static void printResponseBody(Response response) {
        try {
            System.out.println("--BODY--");
            TypedInput body = response.getBody();
            System.out.println("LENGTH: " + body.length());
            System.out.println("-CONTENT-");
            System.out.println(inputStreamToString(body.in()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getHttpAuthHeader(String username, String password) {
        return "Basic " + Base64.encodeToString(String.format("%s:%s", username, password).getBytes(), Base64.NO_WRAP);
    }

    public static String inputStreamToString(InputStream i) throws IOException {
        StringBuilder output = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(i));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            output.append(inputLine).append("\n");
        }
        in.close();
        return output.toString();
    }
}
