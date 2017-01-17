package com.ddiehl.android.htn.view.markdown;

import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import static android.text.Html.FROM_HTML_MODE_LEGACY;

public class HtmlProcessor {

    public Spanned convert(String text) {
        SpannableStringBuilder formatted;

        // Convert HTML using Html.fromHtml, depending on the device platform
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            formatted = new SpannableStringBuilder(
                    Html.fromHtml(text, FROM_HTML_MODE_LEGACY)
            );
        } else {
            //noinspection deprecation
            formatted = new SpannableStringBuilder(
                    Html.fromHtml(text)
            );
        }

        // Strip any trailing new lines
        trimTrailingNewLines(formatted);

        return formatted;
    }

    void trimTrailingNewLines(@NonNull SpannableStringBuilder text) {
        while ("\n".equals(text.subSequence(text.length() - 1, text.length()).toString())) {
            text.delete(text.length() - 1, text.length());
        }
    }
}
