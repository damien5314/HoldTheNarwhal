/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.ddiehl.android.htn.R;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import retrofit.RetrofitError;


public class BaseUtils {
    private static final String TAG = BaseUtils.class.getSimpleName();

    public static void showError(Context context, RetrofitError error) {
        Log.e(TAG, "RetrofitError: " + error.getKind().toString());
        Log.d(TAG, Log.getStackTraceString(error));
        retrofit.client.Response response = error.getResponse();
        if (response != null) {
            switch (response.getStatus()) {
                default:
                    Toast.makeText(context, "An error has occurred (" + response.getStatus() + ")",
                            Toast.LENGTH_LONG).show();
                    break;

            }
        }
    }

    public static String getFriendlyError(Context c, RetrofitError error) {
        retrofit.client.Response response = error.getResponse();
        if (response == null) {
            return c.getString(R.string.error_network_unavailable);
        } else {
            switch (response.getStatus()) {
                case 404:
                    return c.getString(R.string.error_404);
                case 500:
                    return c.getString(R.string.error_500);
                case 503:
                    return c.getString(R.string.error_503);
                case 520:
                    return c.getString(R.string.error_520);
                default:
                    String errorMsg = c.getString(R.string.error_xxx);
                    return String.format(errorMsg, response.getStatus());
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
            Log.d(TAG, "URL: " + response.request().urlString()
                    + " (STATUS: " + response.code() + ")");
        }
    }

    public static void printResponseHeaders(Response response) {
        if (response != null) {
            Log.d(TAG, "--HEADERS--");
            Headers headers = response.headers();
            Log.d(TAG, headers.toString());
        }
    }

    public static void printResponseBody(Response response) {
        if (response != null) {
            try {
                ResponseBody body = response.body();
                Log.d(TAG, "--BODY-- LENGTH: " + body.bytes().length);
                InputStream in_s = body.byteStream();
                Log.d(TAG, getStringFromInputStream(in_s));
                in_s.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getStringFromInputStream(InputStream i) {
        Scanner s = new Scanner(i).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static String getTextFromFile(File file) throws IOException {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append("\n");
                    line = br.readLine();
                }
                return sb.toString();
            } finally {
                br.close();
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Unable to find file: " + file.getAbsolutePath());
            e.printStackTrace();
        }
        return null;
    }

    public static String getRandomString() {
        return UUID.randomUUID().toString();
    }

    // http://www.mkyong.com/java/java-md5-hashing-example/
    public static String getMd5HexString(@NonNull String s) {
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

    public static Intent getNewEmailIntent(String address, String subject, String body, String cc) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { address });
        intent.putExtra(Intent.EXTRA_TEXT, body);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_CC, cc);
        intent.setType("message/rfc822");
        return intent;
    }

    public static long getBuildTime(Context c) {
        ZipFile zf = null;
        try {
            ApplicationInfo ai = c.getPackageManager().getApplicationInfo(c.getPackageName(), 0);
            zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            return ze.getTime();
        } catch (IOException e) {
            Log.e(TAG, "Exception while getting build time", e);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Unable to find package name: " + c.getPackageName(), e);
        } finally {
            try {
                if (zf != null) {
                    zf.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error while closing ZipFile", e);
            }
        }
        return -1;
    }

    public static String getBuildTimeFormatted(Context c) {
        long t = getBuildTime(c);
        return SimpleDateFormat.getDateInstance().format(new java.util.Date(t));
    }

    public static float getScreenWidth(Context c) {
        Display display = ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        float density = c.getResources().getDisplayMetrics().density;
//        float dpHeight = outMetrics.heightPixels / density;
        return outMetrics.widthPixels / density;
    }
}
