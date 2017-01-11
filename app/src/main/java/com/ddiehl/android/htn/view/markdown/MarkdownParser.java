package com.ddiehl.android.htn.view.markdown;

import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;

import com.ddiehl.android.htn.view.URLSpanNoUnderline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.uncod.android.bypass.Bypass;
import timber.log.Timber;

public class MarkdownParser {

    Bypass mBypass;

    public MarkdownParser(Bypass bypass) {
        mBypass = bypass;
    }

    static final Pattern URL_PATTERN_WITH_PROTOCOL = Pattern.compile(
            "\\(*\\b((https?|ftp|file)://)[a-zA-Z-]*\\.[-a-zA-Z0-9+&@#/%?=~_|!:,.;(]*[-a-zA-Z0-9+&@#/%=~_|)]",
            Pattern.MULTILINE
    );

    static final Pattern URL_PATTERN_NO_PROTOCOL = Pattern.compile(
            "\\(*\\bwww\\.[a-zA-Z-]*\\.([-a-zA-Z0-9+&@#/%?=~_|!:,;(]*|\\.?)[-a-zA-Z0-9+&@#/%=~_|)]",
            Pattern.MULTILINE
    );

    static final Pattern REDDIT_LINK_PATTERN = Pattern.compile(
            "\\(?(?:(\\b|/))/?[ru]/[\\p{Alnum}_-]*([-a-zA-Z0-9+&@#/%?=~_|!:,;(]*|\\.?)[-a-zA-Z0-9+&@#/%=~_|)]",
            Pattern.MULTILINE
    );

    /*
        (1) Pre-parse the unformatted markdown text, searching for matches against our Linkify patterns
            (1) For each match, remove the underscores while caching their positions in the string
            (2) Store each link in a Map of links to the list of removed underscores
        (2) Process markdown text through Bypass
        (3) Remove URLSpans within outer URLSpans
        (4) For each link in the Map created in step (1), search for all matches.
            (1) Restore the underscores to both the link text and its URLSpan using its list of underscores.
     */

    Map<String, List<Integer>> processUnderscoresInLinks(String input) {
        // Create StringBuilder for removing underscores easily
        StringBuilder text = new StringBuilder(input);

        // Map of links to a list of the positions of removed underscores within the links
        Map<String, List<Integer>> linkMap = new HashMap<>();

        // Compile list of link markers for each pattern
        List<LinkMarker> links = new ArrayList<>();
        links.addAll(getMarkersForPattern(input, URL_PATTERN_WITH_PROTOCOL));
        links.addAll(getMarkersForPattern(input, URL_PATTERN_NO_PROTOCOL));
        links.addAll(getMarkersForPattern(input, REDDIT_LINK_PATTERN));

        // Remove LinkMarkers occurring within other LinkMarkers, so we don't process the same text twice
        links = cleanListOfMarkers(links);



        return linkMap;
    }

    List<LinkMarker> cleanListOfMarkers(final List<LinkMarker> links) {
        List<LinkMarker> clean = new ArrayList<>(links);

        // FIXME
        // This currently runs in o(n^2), see if we can optimize it somehow so we don't cause
        // bottlenecks in parsing markdown with a lot of URLSpans
        for (int i = 0; i < clean.size(); i++) {
            LinkMarker marker = clean.get(i);

            Timber.d("SPAN(%d - %d)", marker.start, marker.end);

            // Look ahead to spans that start before the end of this span, and remove them
            for (int j = 0; j < clean.size(); j++) {
                LinkMarker nextMarker = clean.get(j);
                // If we're at the same span, move onto the next one
                if (marker == nextMarker) {
                    continue;
                }

                // Check if this span starts before the last one ends
                if (nextMarker.start >= marker.start && nextMarker.start <= marker.end) {
                    Timber.d("This span starts and ends within the current span, so remove it");
                    // Remove span and null it out so we don't check it later
                    clean.remove(nextMarker);
                }
            }
        }

        return clean;
    }

    List<LinkMarker> getMarkersForPattern(final String text, final Pattern pattern) {
        // List of indices of matches against a link regex
        // We want to cache a list of indices and do all of the removal later
        List<LinkMarker> links = new ArrayList<>();

        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            LinkMarker marker = new LinkMarker(matcher.start(), matcher.end());
            links.add(marker);
        }

        return links;
    }

    static class LinkMarker {
        public int start;
        public int end;

        public LinkMarker(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    public CharSequence convert(String text) {

        Map<String, List<Integer>> linkMap = processUnderscoresInLinks(text);


        CharSequence markdown = mBypass.markdownToSpannable(text);
        SpannableStringBuilder formatted = new SpannableStringBuilder(markdown);

        // Pre-parse the formatted string for matches that are going to be linkified, removing
        // any StyleSpans (probably italicized sections from _underscores_)
        removeStyleSpansFromLinksMatchingPattern(formatted, URL_PATTERN_WITH_PROTOCOL);
        removeStyleSpansFromLinksMatchingPattern(formatted, URL_PATTERN_NO_PROTOCOL);
        removeStyleSpansFromLinksMatchingPattern(formatted, REDDIT_LINK_PATTERN);

        // Add links for URLs with a protocol
        Linkify.addLinks(formatted, URL_PATTERN_WITH_PROTOCOL, null);

        // Add links with `https://` prepended to links without a protocol
        Linkify.addLinks(formatted, URL_PATTERN_NO_PROTOCOL, null, null,
                (match, url) -> "https://" + url);

        // Linkify links for /r/ and /u/ patterns
        Linkify.addLinks(
                formatted, REDDIT_LINK_PATTERN, null, null,
                (match, url) -> {
                    url = url.trim();
                    if (!url.startsWith("/") && !url.startsWith("(/")) {
                        url = "/" + url;
                    }
                    return "https://www.reddit.com" + url;
                }
        );

        // Clear up anything we might have double-linked
        removeLinksWithinLinks(formatted);

        // Remove parentheses from links that are surrounded with them
        fixLinksSurroundedWithParentheses(formatted);

        convertUrlSpansToNoUnderlineForm(formatted);

        return formatted;
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
                int spanStart = text.getSpanStart(styleSpan);
                int spanEnd = text.getSpanEnd(styleSpan);
                // Don't remove StyleSpans that cover the FULL match
                if (spanStart > matcher.start() || spanEnd < matcher.end()) {
                    // Add indices to index cache
                    indices.add(spanStart);
                    indices.add(spanEnd);
                    text.removeSpan(styleSpan);
                }
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

    void fixLinksSurroundedWithParentheses(SpannableStringBuilder text) {
        URLSpan[] urlSpans = text.getSpans(0, text.length(), URLSpan.class);

        for (URLSpan urlSpan : urlSpans) {
            int spanStart = text.getSpanStart(urlSpan);
            int spanEnd = text.getSpanEnd(urlSpan);
            String linkText = text.subSequence(spanStart, spanEnd).toString();

            if (linkText.startsWith("(") && linkText.endsWith(")")) {
                StringBuilder url = new StringBuilder(urlSpan.getURL());

                // https://www.reddit.com/(r/cats)
                int startIndex = url.indexOf(linkText);
                int endIndex = startIndex + linkText.length() - 1;

                url.deleteCharAt(endIndex);
                url.deleteCharAt(startIndex);

//                String newUrl = url.substring(1, url.length() - 1);

                text.removeSpan(urlSpan);
//                text.setSpan(new URLSpan(newUrl), spanStart + 1, spanEnd - 1, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
                text.setSpan(new URLSpan(url.toString()), spanStart + 1, spanEnd - 1, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    void convertUrlSpansToNoUnderlineForm(SpannableStringBuilder text) {
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
