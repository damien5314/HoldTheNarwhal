package com.ddiehl.android.htn.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.ddiehl.android.htn.HoldTheNarwhal;
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


public class BaseUtils {
  public static void showError(@NonNull Context context, @Nullable retrofit.Response error) {
    String code = error == null ? "NULL" : String.valueOf(error.code());
    HoldTheNarwhal.getLogger().d(String.format("Retrofit error (STATUS %s)", code));
    Toast.makeText(context,
        String.format("An error has occurred (%s)", code), Toast.LENGTH_LONG).show();
  }

  @NonNull
  public static String getFriendlyError(@Nullable retrofit.Response response) {
    Context context = HoldTheNarwhal.getContext();
    if (response == null) {
      return context.getString(R.string.error_network_unavailable);
    } else {
      switch (response.code()) {
        case 404:
          return context.getString(R.string.error_404);
        case 500:
          return context.getString(R.string.error_500);
        case 503:
          return context.getString(R.string.error_503);
        case 520:
          return context.getString(R.string.error_520);
        default:
          String errorMsg = context.getString(R.string.error_xxx);
          return String.format(errorMsg, response.code());
      }
    }
  }

  public static void printResponse(@Nullable Response response) {
    printResponseStatus(response);
    printResponseHeaders(response);
    printResponseBody(response);
  }

  public static void printResponseStatus(@Nullable Response response) {
    if (response != null) {
      HoldTheNarwhal.getLogger().d(String.format("URL: %s (STATUS: %s)",
          response.request().urlString(), response.code()));
    }
  }

  public static void printResponseHeaders(@Nullable Response response) {
    if (response != null) {
      HoldTheNarwhal.getLogger().d("--HEADERS--");
      Headers headers = response.headers();
      HoldTheNarwhal.getLogger().d(headers.toString());
    }
  }

  public static void printResponseBody(@Nullable Response response) {
    if (response != null) {
      try {
        ResponseBody body = response.body();
        HoldTheNarwhal.getLogger().d("--BODY-- LENGTH: " + body.bytes().length);
        InputStream in_s = body.byteStream();
        HoldTheNarwhal.getLogger().d(getStringFromInputStream(in_s));
        in_s.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @NonNull
  public static String getStringFromInputStream(@NonNull InputStream i) {
    Scanner s = new Scanner(i).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }

  @Nullable
  public static String getTextFromFile(@NonNull File file) throws IOException {
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
      HoldTheNarwhal.getLogger().e("Unable to find file: " + file.getAbsolutePath());
      e.printStackTrace();
    }
    return null;
  }

  @NonNull
  public static String getRandomString() {
    return UUID.randomUUID().toString();
  }

  // http://www.mkyong.com/java/java-md5-hashing-example/
  @Nullable
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
      HoldTheNarwhal.getLogger().w("Unable to obtain MD5 hash");
      e.printStackTrace();
    }

    return null;
  }

  @NonNull
  public static Intent getNewEmailIntent(@Nullable String address, @Nullable String subject,
                       @Nullable String body, @Nullable String cc) {
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.putExtra(Intent.EXTRA_EMAIL, new String[] { address });
    intent.putExtra(Intent.EXTRA_TEXT, body);
    intent.putExtra(Intent.EXTRA_SUBJECT, subject);
    intent.putExtra(Intent.EXTRA_CC, cc);
    intent.setType("message/rfc822");
    return intent;
  }

  public static long getBuildTime(@NonNull Context c) {
    ZipFile zf = null;
    try {
      ApplicationInfo ai = c.getPackageManager().getApplicationInfo(c.getPackageName(), 0);
      zf = new ZipFile(ai.sourceDir);
      ZipEntry ze = zf.getEntry("classes.dex");
      return ze.getTime();
    } catch (IOException e) {
      HoldTheNarwhal.getLogger().e("Exception while getting build time", e);
    } catch (PackageManager.NameNotFoundException e) {
      HoldTheNarwhal.getLogger().e("Unable to find package name: " + c.getPackageName(), e);
    } finally {
      try {
        if (zf != null) {
          zf.close();
        }
      } catch (Exception e) {
        HoldTheNarwhal.getLogger().e("Error while closing ZipFile", e);
      }
    }
    return -1;
  }

  @NonNull
  public static String getBuildTimeFormatted(@NonNull Context c) {
    long t = getBuildTime(c);
    return SimpleDateFormat.getDateInstance().format(new java.util.Date(t));
  }

  public static float getScreenWidth(@NonNull Context c) {
    WindowManager wm = ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE));
    Display display = wm.getDefaultDisplay();
    DisplayMetrics outMetrics = new DisplayMetrics ();
    display.getMetrics(outMetrics);

    float density = c.getResources().getDisplayMetrics().density;
//    float dpHeight = outMetrics.heightPixels / density;
    return outMetrics.widthPixels / density;
  }

  public static int getNumberOfDigits(int n) {
    if (n < 0) n *= -1;
    if (n == 0) return 0;
    return (int) Math.log10(n) + 1;
  }

  /**
   * http://stackoverflow.com/a/9563438/3238938
   * This method converts dp unit to equivalent pixels, depending on device density.
   *
   * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
   * @return A float value to represent px equivalent to dp depending on device density
   */
  public static float convertDpToPixel(float dp) {
    Resources resources = Resources.getSystem();
    DisplayMetrics metrics = resources.getDisplayMetrics();
    return dp * (metrics.densityDpi / 160f);
  }

  /**
   * http://stackoverflow.com/a/9563438/3238938
   * This method converts device specific pixels to density independent pixels.
   *
   * @param px A value in px (pixels) unit. Which we need to convert into db
   * @return A float value to represent dp equivalent to px value
   */
  public static float convertPixelsToDp(float px) {
    Resources resources = Resources.getSystem();
    DisplayMetrics metrics = resources.getDisplayMetrics();
    return px / (metrics.densityDpi / 160f);
  }
}
