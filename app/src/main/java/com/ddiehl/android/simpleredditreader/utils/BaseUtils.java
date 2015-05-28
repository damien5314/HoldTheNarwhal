package com.ddiehl.android.simpleredditreader.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.InputStream;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;
import retrofit.mime.TypedInput;


public class BaseUtils {
    private static final String TAG = BaseUtils.class.getSimpleName();

    public static void showError(Context context, RetrofitError error) {
        Log.e(TAG, "RetrofitError: " + error.getKind().toString());
        Log.d(TAG, Log.getStackTraceString(error));
        Response response = error.getResponse();
        if (response != null) {
            switch (response.getStatus()) {
                default:
                    Toast.makeText(context, "An error has occurred (" + response.getStatus() + ")",
                            Toast.LENGTH_LONG).show();
                    break;

            }
        }
    }

    public static void printResponse(Response response) {
        printResponseStatus(response);
        printResponseHeaders(response);
        printResponseBody(response);
    }

    public static void printResponseStatus(Response response) {
        if (response != null) {
            Log.d(TAG, "URL: " + response.getUrl() + " (STATUS: " + response.getStatus() + ")");
        }
    }

    public static void printResponseHeaders(Response response) {
        if (response != null) {
            Log.d(TAG, "--HEADERS--");
            List<Header> headersList = response.getHeaders();
            for (Header header : headersList) {
                Log.d(TAG, header.toString());
            }
        }
    }

    public static void printResponseBody(Response response) {
        if (response != null) {
            try {
                TypedInput body = response.getBody();
                Log.d(TAG, "--BODY-- LENGTH: " + body.length());
                InputStream in_s = body.in();
                Log.d(TAG, inputStreamToString(in_s));
                in_s.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String inputStreamToString(InputStream i) {
        Scanner s = new Scanner(i).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static String getRandomString() {
        return UUID.randomUUID().toString();
    }
}
