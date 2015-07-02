/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.ddiehl.android.htn.R;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    public static void showFriendlyError(Context context, RetrofitError error) {
        Log.e(TAG, "RetrofitError: " + error.getKind().toString());
        Log.d(TAG, Log.getStackTraceString(error));
        Response response = error.getResponse();
        if (response != null) {
            switch (response.getStatus()) {
                case 404:
                    Toast.makeText(context, R.string.error_404, Toast.LENGTH_SHORT).show();
                case 503:
                    Toast.makeText(context, R.string.error_503, Toast.LENGTH_SHORT).show();
                default:
                    Toast.makeText(context, String.format(context.getString(R.string.error_xxx),
                            response.getStatus()), Toast.LENGTH_SHORT).show();
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

    // http://www.mkyong.com/java/java-md5-hashing-example/
    public static String getMd5HexString(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(s.getBytes());
            byte byteData[] = md.digest();

            // Convert bytes to hex format
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.w(TAG, "Unable to obtain MD5 hash");
            e.printStackTrace();
        }

        return null;
    }
}
