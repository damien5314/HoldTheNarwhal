package com.ddiehl.android.htn.view.markdown;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.text.style.URLSpanNoUnderline;

import com.ddiehl.android.htn.utils.AndroidUtils;

import static android.text.Html.FROM_HTML_MODE_LEGACY;

public class HtmlProcessor {

    Context mContext;

    public HtmlProcessor(Context context) {
        mContext = context;
    }

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

        fixUrlSpansForRedditLinks(formatted);

        // Convert URLSpans to no underline form
        AndroidUtils.convertUrlSpansToNoUnderlineForm(formatted);

        AndroidUtils.convertQuoteSpansToCustom(mContext, formatted);

        return formatted;
    }

    void trimTrailingNewLines(@NonNull SpannableStringBuilder text) {
        while ("\n".equals(text.subSequence(text.length() - 1, text.length()).toString())) {
            text.delete(text.length() - 1, text.length());
        }
    }

    void fixUrlSpansForRedditLinks(@NonNull SpannableStringBuilder text) {
        // Get all URLSpans
        URLSpan[] spans = text.getSpans(0, text.length(), URLSpan.class);

        for (URLSpan span : spans) {
            String url = span.getURL();

            // Detect if the span is for a reddit link
            if (url.startsWith("/r/") || url.startsWith("/u/")) {
                // Cache start and end markers
                int start = text.getSpanStart(span);
                int end = text.getSpanEnd(span);
                // Prepend the hostname to the linked one
                String newUrl = "https://www.reddit.com" + url;
                // Remove old span and set new one
                text.removeSpan(span);
                text.setSpan(new URLSpanNoUnderline(newUrl), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }
}
