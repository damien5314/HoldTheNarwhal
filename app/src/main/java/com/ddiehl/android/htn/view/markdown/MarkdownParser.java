package com.ddiehl.android.htn.view.markdown;

import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;

import com.ddiehl.android.htn.view.DLinkify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.uncod.android.bypass.Bypass;
import timber.log.Timber;

public class MarkdownParser {

    Bypass mBypass;

    public MarkdownParser(Bypass bypass) {
        mBypass = bypass;
    }

    static final Pattern THE_PATTERN_WITH_PROTOCOL = Pattern.compile(
            "\\(*\\b((https?|ftp|file)://)[a-zA-Z-]*\\.[-a-zA-Z0-9+&@#/%?=~_|!:,.;(]*[-a-zA-Z0-9+&@#/%=~_|)]",
            Pattern.MULTILINE
    );

    static final Pattern THE_PATTERN_NO_PROTOCOL = Pattern.compile(
            "\\(*\\b[a-zA-Z-]*\\.[-a-zA-Z0-9+&@#/%?=~_|!:,.;(]*[-a-zA-Z0-9+&@#/%=~_|)]",
            Pattern.MULTILINE
    );

    static final Pattern REDDIT_LINK_PATTERN = Pattern.compile(
            "(?:(^|/))/?[ru]/[\\p{Alnum}_-]*", Pattern.MULTILINE
    );

    public CharSequence convert(String text) {

        CharSequence markdown = mBypass.markdownToSpannable(text);
        SpannableStringBuilder formatted = new SpannableStringBuilder(markdown);

        // Pre-parse the formatted string for matches that are going to be linkified, removing
        // any StyleSpans (probably italicized sections from _underscores_)
        removeStyleSpansFromLinksMatchingPattern(formatted, THE_PATTERN_WITH_PROTOCOL);
        removeStyleSpansFromLinksMatchingPattern(formatted, THE_PATTERN_NO_PROTOCOL);
        removeStyleSpansFromLinksMatchingPattern(formatted, REDDIT_LINK_PATTERN);

        // Add links for URLs with a protocol
        DLinkify.addLinks(formatted, THE_PATTERN_WITH_PROTOCOL, null);

        // Add links with `https://` prepended to links without a protocol
        DLinkify.addLinks(formatted, THE_PATTERN_NO_PROTOCOL, null, null,
                (match, url) -> "https://" + url);

        // Linkify links for /r/ and /u/ patterns
        DLinkify.addLinks(
                formatted, REDDIT_LINK_PATTERN, null, null,
                (match, url) -> {
                    url = url.trim();
                    if (!url.startsWith("/")) {
                        url = "/" + url;
                    }
                    return "https://www.reddit.com" + url;
                }
        );

        // Clear up anything we might have double-linked
        removeLinksWithinLinks(formatted);

        // Remove parentheses from links that are surrounded with them
        fixLinksSurroundedWithParentheses(formatted);

        return formatted;
    }

    private void fixLinksSurroundedWithParentheses(SpannableStringBuilder text) {
        URLSpan[] urlSpans = text.getSpans(0, text.length(), URLSpan.class);

        for (URLSpan urlSpan : urlSpans) {
            int spanStart = text.getSpanStart(urlSpan);
            int spanEnd = text.getSpanEnd(urlSpan);
            String linkText = text.subSequence(spanStart, spanEnd).toString();

            if (linkText.startsWith("(") && linkText.endsWith(")")) {
                String url = urlSpan.getURL();
                String newUrl = url.substring(1, url.length() - 1);

                text.removeSpan(urlSpan);
                text.setSpan(new URLSpan(newUrl), spanStart + 1, spanEnd - 1, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    void removeStyleSpansFromLinksMatchingPattern(
            @NonNull SpannableStringBuilder text, @NonNull Pattern pattern) {
        Timber.d("[DCD] PATTERN " + pattern.toString());

        Matcher matcher = pattern.matcher(text);

        List<Integer> indices = new ArrayList<>();
        int matchCount = 0;

        while (matcher.find()) {
            Timber.d("[DCD] Match[%d-%d]: %s", matcher.start(), matcher.end(), text.subSequence(matcher.start(), matcher.end()));
            matchCount++;
            StyleSpan[] styleSpans = text.getSpans(matcher.start(), matcher.end(), StyleSpan.class);
            for (StyleSpan styleSpan : styleSpans) {
                indices.add(text.getSpanStart(styleSpan));
                indices.add(text.getSpanEnd(styleSpan));
//                text.insert(text.getSpanStart(styleSpan), "_");
//                text.insert(text.getSpanEnd(styleSpan), "_");
                text.removeSpan(styleSpan);
            }
        }
        Timber.d("[DCD] MATCH COUNT: " + matchCount);

        // `getSpans` does not return spans in the order they appear in the string it seems?
        // So the matched indices end up coming out of order
        Collections.sort(indices);

        // Now take the list of indices and actually apply the results, in reverse order
        // TODO Maybe do this more efficiently if the array keeps having to shift earlier modifications
        for (int i = indices.size() - 1; i >= 0; i--) {
            int index = indices.get(i);
            text.insert(index, "_");
        }
    }

    void removeLinksWithinLinks(SpannableStringBuilder formatted) {
        URLSpan[] urlSpans = formatted.getSpans(0, formatted.length(), URLSpan.class);

        // FIXME
        // This currently runs in o(n^2), see if we can optimize it somehow so we don't cause
        // bottlenecks in parsing markdown with a lot of URLSpans
        for (int i = 0; i < urlSpans.length; i++) {
            URLSpan urlSpan = urlSpans[i];
            if (urlSpan == null) {
                continue;
            }

            int spanStart = formatted.getSpanStart(urlSpan);
            int spanEnd = formatted.getSpanEnd(urlSpan);
            Timber.d("SPAN(%d - %d)", spanStart, spanEnd);

            // Look ahead to spans that start before the end of this span, and remove them
            for (int j = 0; j < urlSpans.length; j++) {
                URLSpan nextSpan = urlSpans[j];
                // If we nulled out the span before, or we're at the same span, move onto the next one
                if (nextSpan == null || urlSpan == nextSpan) {
                    continue;
                }

                // Check if this span starts before the last one ends
                int nextSpanStart = formatted.getSpanStart(nextSpan);
                int nextSpanEnd = formatted.getSpanEnd(nextSpan);
//                Timber.d("NEXT(%d - %d)", nextSpanStart, nextSpanEnd);

                if (nextSpanStart >= spanStart && nextSpanStart <= spanEnd) {
                    Timber.d("This span starts and ends within the current span, so remove it");
                    // Remove span and null it out so we don't check it later
                    formatted.removeSpan(nextSpan);
                    urlSpans[j] = null;
                }
            }
        }
    }

    void autolink(StringBuilder builder, Pattern pattern) {
        List<Integer> matchIndices = new ArrayList<>();
        Matcher matcher = pattern.matcher(builder);
        while (matcher.find()) {
            matchIndices.add(matcher.start());
            matchIndices.add(matcher.end());
        }

        int i = matchIndices.size() - 1;
        while (i > 0) {
            builder.insert(matchIndices.get(i--), ">");
            builder.insert(matchIndices.get(i--), "<");
        }
    }
}
