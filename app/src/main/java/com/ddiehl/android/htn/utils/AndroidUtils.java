package com.ddiehl.android.htn.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BulletSpan;
import android.text.style.QuoteSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.ddiehl.android.htn.BuildConfig;
import com.ddiehl.android.htn.view.text.CustomBulletSpan;
import com.ddiehl.android.htn.view.text.CustomQuoteSpan;
import com.ddiehl.android.htn.view.text.NoUnderlineURLSpan;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @NotNull
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

    @NotNull
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

    public static Drawable getAttrTintedDrawable(
            Context context, @DrawableRes int drawableResId, @AttrRes int colorAttrResId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableResId);
        int color = ThemeUtilsKt.getColorFromAttr(context, colorAttrResId);
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        return drawable;
    }

    public static void safeStartActivity(@Nullable Context context, @NotNull Intent intent) {
        if (context == null) return;

        final PackageManager packageManager = context.getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            context.startActivity(intent);
            return;
        }

        Timber.e(new RuntimeException("Unable to handle this Uri: " + intent.getDataString()));
    }

    public static void convertUrlSpansToNoUnderlineForm(SpannableStringBuilder text) {
        URLSpan[] urlSpans = text.getSpans(0, text.length(), URLSpan.class);

        for (URLSpan urlSpan : urlSpans) {
            if (urlSpan instanceof NoUnderlineURLSpan) {
                // Already correct type
                continue;
            }

            int start = text.getSpanStart(urlSpan);
            int end = text.getSpanEnd(urlSpan);
            String url = urlSpan.getURL();

            text.removeSpan(urlSpan);
            text.setSpan(new NoUnderlineURLSpan(url), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public static void convertQuoteSpansToCustom(
            @NotNull SpannableStringBuilder text, @ColorInt int color) {
        QuoteSpan[] spans = text.getSpans(0, text.length(), QuoteSpan.class);

        for (QuoteSpan span : spans) {
            int start = text.getSpanStart(span);
            int end = text.getSpanEnd(span);

            CustomQuoteSpan newSpan = new CustomQuoteSpan(color);

            text.removeSpan(span);
            text.setSpan(newSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public static void convertBulletSpansToCustom(@NotNull SpannableStringBuilder text) {
        BulletSpan[] spans = text.getSpans(0, text.length(), BulletSpan.class);

        for (BulletSpan span : spans) {
            int start = text.getSpanStart(span);
            int end = text.getSpanEnd(span);

            text.removeSpan(span);
            text.setSpan(new CustomBulletSpan(16), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public static @NotNull TextInputLayout getTextInputLayout(@NotNull TextInputEditText editText) {
        ViewParent parent = editText.getParent();

        // Keep searching until we reach the top of the view stack if necessary
        while (parent != null) {
            if (parent instanceof TextInputLayout) {
                return (TextInputLayout) parent;
            } else {
                parent = parent.getParent();
            }
        }

        // If we didn't find a TextInputLayout, throw an exception
        throw new RuntimeException("no TextInputLayout found");
    }
}
