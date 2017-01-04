package com.ddiehl.android.htn.view.markdown;

import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;

import com.ddiehl.android.htn.view.Linkify;

import java.util.ArrayList;
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

    public CharSequence convert(String text) {
        Pattern redditLinkMatcher = Pattern.compile(
                "(?:(^|/))/?[ru]/[^\\s\\)]*", Pattern.MULTILINE
        );
        Pattern httpProtocolMatcher = Pattern.compile(
                "(?:^)((http)s?://)?www\\.[^\\s\\)]*", Pattern.MULTILINE
        );
        Pattern noProtocolMatcher = Pattern.compile(
                "(?:^)((http)s?://){0}www\\.[^\\s\\)]*", Pattern.MULTILINE
        );
        Pattern anyProtocolMatcher = Pattern.compile("[a-z]+://[^ \\n]*");

        // Find links in the text and surround them with autolink tags '<>' before processing
//            autolink(sb, redditLinkMatcher);
//            autolink(sb, httpProtocolMatchers);
//            autolink(sb, anyProtocolMatcher);

        CharSequence markdown = mBypass.markdownToSpannable(text);
        SpannableStringBuilder formatted = new SpannableStringBuilder(markdown);

        // Pre-parse the formatted string for matches that are going to be linkified, removing
        // any StyleSpans (probably italicized sections from _underscores_)
        Matcher matcher = redditLinkMatcher.matcher(formatted);
        while (matcher.find()) {
            StyleSpan[] styleSpans = formatted.getSpans(matcher.start(), matcher.end(), StyleSpan.class);
            for (StyleSpan styleSpan : styleSpans) {
                formatted.insert(formatted.getSpanStart(styleSpan), "_");
                formatted.insert(formatted.getSpanEnd(styleSpan), "_");
                formatted.removeSpan(styleSpan);
            }
        }

        // Linkify links for /r/ and /u/ patterns
        Linkify.addLinks(
                formatted, redditLinkMatcher, null, null,
                (match, url) -> {
                    url = url.trim();
                    if (!url.startsWith("/")) {
                        url = "/" + url;
                    }
                    return "https://www.reddit.com" + url;
                }
        );

        Matcher matcher2 = httpProtocolMatcher.matcher(formatted);
        while (matcher2.find()) {
            StyleSpan[] styleSpans = formatted.getSpans(matcher2.start(), matcher2.end(), StyleSpan.class);
            for (StyleSpan styleSpan : styleSpans) {
                formatted.insert(formatted.getSpanStart(styleSpan), "_");
                formatted.insert(formatted.getSpanEnd(styleSpan), "_");
                formatted.removeSpan(styleSpan);
            }
        }

        // Add links missing protocol
        Linkify.addLinks(
                formatted, httpProtocolMatcher, null, null,
                (match, url) -> {
                    return url.trim();
                }
        );

        // Add links missing protocol
        Linkify.addLinks(
                formatted, noProtocolMatcher, "https://", null,
                (match, url) -> {
                    return url.trim();
                }
        );

        // Add links with any protocol
//            Linkify.addLinks(s, anyProtocolMatcher, null);

        removeFormattingWithinLinks(formatted);
        removeLinksWithinLinks(formatted);

        return formatted;
    }

    void removeFormattingWithinLinks(SpannableStringBuilder string) {
        // Get all URLSpans within our formatted SpannableString
        URLSpan[] spans = string.getSpans(0, string.length(), URLSpan.class);

        // FIXME
        // this method is screwing up spans that have a different URL than the actual text
        // (such as "r/Android")

        // For each URLSpan within the full string
        for (URLSpan urlSpan : spans) {
            // Cache the start and end of the span
            int start = string.getSpanStart(urlSpan);
            int end = string.getSpanEnd(urlSpan);

            // Find any StyleSpans within the URLSpan (always? caused because of underscores)
            StyleSpan[] innerSpans = string.getSpans(start, end, StyleSpan.class);
            for (StyleSpan innerSpan : innerSpans) {
                // Add an underscore to the string at the start of the StyleSpan
                int spanStart = string.getSpanStart(innerSpan);
                string.insert(spanStart, "_");
                // Add an underscore to the string at the end of the StyleSpan
                int spanEnd = string.getSpanEnd(innerSpan);
                string.insert(spanEnd, "_");
                // Remove the StyleSpan from the string
                string.removeSpan(innerSpan);
            }

            // Recalculate the bounds of the URLSpan, since it's been modified
            int correctedSpanStart = string.getSpanStart(urlSpan);
            int correctedSpanEnd = string.getSpanEnd(urlSpan);
            // Get the corrected URL which is the substring from the corrected span's start and end
            CharSequence correctedUrl = string.subSequence(correctedSpanStart, correctedSpanEnd);

            // Remove the old span and add a new one with the corrected URL
//            string.removeSpan(urlSpan);
//            string.setSpan(
//                    new URLSpanNoUnderline(correctedUrl.toString()),
//                    correctedSpanStart, correctedSpanEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE
//            );
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
                Timber.d("NEXT(%d - %d)", nextSpanStart, nextSpanEnd);

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
