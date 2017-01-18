package com.ddiehl.android.htn.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.CustomQuoteSpan;
import android.text.style.QuoteSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

import com.ddiehl.android.htn.BuildConfig;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.URLSpanNoUnderline;

import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Response;
import timber.log.Timber;

public class AndroidUtils {

    public static void printResponseStatus(@Nullable Response response) {
        if (response != null) {
            Timber.d("URL: %s (STATUS: %s)",
                    response.request().url().toString(), response.code());
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

    public static long getBuildTime() {
        return BuildConfig.BUILD_TIME_UTC;
    }

    @NonNull
    public static String getBuildTimeFormatted() {
        long t = getBuildTime() * 1000; // convert to ms
        return SimpleDateFormat.getDateInstance().format(new Date(t));
    }

    public static int getScreenWidthPx() {
        DisplayMetrics display = Resources.getSystem().getDisplayMetrics();
        return display.widthPixels;
    }

    public static int getScreenHeightPx() {
        DisplayMetrics display = Resources.getSystem().getDisplayMetrics();
        return display.heightPixels;
    }

    public static float getScreenWidthDp() {
        DisplayMetrics display = Resources.getSystem().getDisplayMetrics();
        float density = display.density;
        return display.widthPixels / density;
    }

    public static float getScreenHeightDp() {
        DisplayMetrics display = Resources.getSystem().getDisplayMetrics();
        float density = display.density;
        return display.heightPixels / density;
    }

    /**
     * http://stackoverflow.com/a/9563438/3238938
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float dpToPx(float dp) {
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
    public static float pxToDp(float px) {
        Resources resources = Resources.getSystem();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / (metrics.densityDpi / 160f);
    }

    public static int getChildrenInTabLayout(TabLayout l) {
        ViewGroup g = (ViewGroup) l.getChildAt(0);
        if (g == null) return 0;
        return g.getChildCount();
    }

    public static boolean isConnectedToNetwork(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnectedOrConnecting();
    }

    public static Drawable getTintedDrawable(
            Context context, @DrawableRes int drawableResId, @ColorRes int colorResId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableResId);
        int color = ContextCompat.getColor(context, colorResId);
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        return drawable;
    }

    public static void safeStartActivity(@NonNull Context context, @NonNull Intent intent) {
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            // Log incorrect Uri
            RuntimeException exception =
                    new RuntimeException("Unable to handle this Uri: " + intent.getDataString());
            Timber.e(exception);
        }
    }

    public static void convertUrlSpansToNoUnderlineForm(SpannableStringBuilder text) {
        URLSpan[] urlSpans = text.getSpans(0, text.length(), URLSpan.class);

        for (URLSpan urlSpan : urlSpans) {
            if (urlSpan instanceof URLSpanNoUnderline) {
                // Already correct type
                continue;
            }

            int start = text.getSpanStart(urlSpan);
            int end = text.getSpanEnd(urlSpan);
            String url = urlSpan.getURL();

            text.removeSpan(urlSpan);
            text.setSpan(new URLSpanNoUnderline(url), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public static void convertQuoteSpansToCustom(
            @NonNull Context context, @NonNull SpannableStringBuilder text) {
        QuoteSpan[] spans = text.getSpans(0, text.length(), QuoteSpan.class);

        for (QuoteSpan span : spans) {
            int start = text.getSpanStart(span);
            int end = text.getSpanEnd(span);

            @ColorInt int quoteColor = ContextCompat.getColor(context, R.color.markdown_quote_block);
            CustomQuoteSpan newSpan = new CustomQuoteSpan(quoteColor);

            text.removeSpan(span);
            text.setSpan(newSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}
