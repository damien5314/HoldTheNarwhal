package com.ddiehl.android.htn.view.markdown;

import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.utils.AndroidUtils;
import com.ddiehl.android.htn.view.text.CustomBulletSpan;
import com.ddiehl.android.htn.view.text.NoUnderlineURLSpan;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static android.text.Html.FROM_HTML_MODE_LEGACY;

public class HtmlParser {

    Context context;

    public HtmlParser(Context context) {
        this.context = context;
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

        @ColorInt int quoteColor = ContextCompat.getColor(context, R.color.markdown_quote_block);
        AndroidUtils.convertQuoteSpansToCustom(formatted, quoteColor);

        AndroidUtils.convertBulletSpansToCustom(formatted);

        removeBreaksInBetweenConsecutiveBulletSpans(formatted);

        return formatted;
    }

    void removeBreaksInBetweenConsecutiveBulletSpans(@NotNull SpannableStringBuilder text) {
        CustomBulletSpan[] spans = text.getSpans(0, text.length(), CustomBulletSpan.class);
        List<CustomBulletSpan> spanList = Arrays.asList(spans);

        Collections.sort(
                spanList,
                (o1, o2) -> text.getSpanStart(o1) - text.getSpanStart(o2) // Swap these?
        );

        for (int i = spanList.size() - 1; i > 0; i--) {
            // If this span starts 2 characters (newlines) after the one before it
            int spanStart = text.getSpanStart(spanList.get(i));
            int nextSpanEnd = text.getSpanEnd(spanList.get(i - 1));

            if (spanStart - 1 == nextSpanEnd && text.charAt(spanStart - 1) == '\n') {
                // Remove one of the newline characters
                text.delete(spanStart - 1, spanStart);
            }
        }
    }

    void trimTrailingNewLines(@NotNull SpannableStringBuilder text) {
        while (text.length() > 0
                && "\n".equals(text.subSequence(text.length() - 1, text.length()).toString())) {
            text.delete(text.length() - 1, text.length());
        }
    }

    void fixUrlSpansForRedditLinks(@NotNull SpannableStringBuilder text) {
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
                text.setSpan(new NoUnderlineURLSpan(newUrl), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }
}
