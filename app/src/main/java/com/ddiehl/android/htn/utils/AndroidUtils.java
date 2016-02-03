package com.ddiehl.android.htn.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

import com.ddiehl.android.htn.HoldTheNarwhal;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import okhttp3.Response;

public class AndroidUtils {
  public static void printResponseStatus(@Nullable Response response) {
    if (response != null) {
      HoldTheNarwhal.getLogger().d(String.format("URL: %s (STATUS: %s)",
          response.request().url().toString(), response.code()));
    }
  }

  @NonNull
  public static Intent getNewEmailIntent(
      @Nullable String address, @Nullable String subject, @Nullable String body, @Nullable String cc) {
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
      ZipEntry ze = zf.getEntry("AndroidManifest.xml");
      return ze.getTime();
    } catch (IOException e) {
      HoldTheNarwhal.getLogger().e("Exception while getting build time", e);
    } catch (PackageManager.NameNotFoundException e) {
      HoldTheNarwhal.getLogger().e("Unable to find package name: " + c.getPackageName(), e);
    } finally {
      try {
        if (zf != null) zf.close();
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

  public static float getScreenWidth() {
    DisplayMetrics display = Resources.getSystem().getDisplayMetrics();
    float density = display.density;
    return display.widthPixels / density;
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

  public static int getChildrenInTabLayout(TabLayout l) {
    ViewGroup g = (ViewGroup) l.getChildAt(0);
    if (g == null) return 0;
    return g.getChildCount();
  }
}