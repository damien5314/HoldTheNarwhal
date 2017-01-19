package com.ddiehl.android.htn.view.markdown;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.URLSpan;
import android.text.util.Linkify;

import com.ddiehl.android.htn.utils.AndroidUtils;
import com.ddiehl.android.htn.view.text.NoUnderlineURLSpan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.uncod.android.bypass.Bypass;

public class MarkdownParser {

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

    Bypass mBypass;

    public MarkdownParser(Bypass bypass) {
        mBypass = bypass;
    }

    public CharSequence convert(String text) {

        StringBuilder result = new StringBuilder(text);

        // Remove all underscores from the input string and get a map of the links within the string
        // to the positions of the underscores within the links.
        Map<String, List<Integer>> linkMap = processUnderscoresInLinks(result);

        // Process markdown for the cleaned string as usual
        // Underscores have all been removed, therefore should not interfere with the formatting.
        CharSequence markdown = mBypass.markdownToSpannable(result.toString());
        SpannableStringBuilder formatted = new SpannableStringBuilder(markdown);

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

        // Restore underscores to the formatted string using the map we previously made
        restoreUnderscoresToText(formatted, linkMap);

        // Convert normal URLSpans to the NoUnderline form
        AndroidUtils.convertUrlSpansToNoUnderlineForm(formatted);

        return formatted;
    }

    Map<String, List<Integer>> processUnderscoresInLinks(StringBuilder input) {
        // Create a copy of the input text so we can modify it
        // while still pulling substrings from the original string.
        StringBuilder text = new StringBuilder(input);

        // Map of links to a list of the positions of removed underscores within the links
        Map<String, List<Integer>> linkMap = new HashMap<>();

        // Compile list of link markers for each pattern
        List<LinkMarker> markers = new ArrayList<>();
        markers.addAll(getMarkersForPattern(input, URL_PATTERN_WITH_PROTOCOL));
        markers.addAll(getMarkersForPattern(input, URL_PATTERN_NO_PROTOCOL));
        markers.addAll(getMarkersForPattern(input, REDDIT_LINK_PATTERN));

        // Remove LinkMarkers occurring within other LinkMarkers, so we don't process the same text twice
        markers = cleanListOfMarkers(markers);

        // Sort the list of markers in reverse order of appearance in the input
        Collections.sort(markers, (m1, m2) -> m2.start - m1.start);

        for (LinkMarker marker : markers) {
            // Get indices of all underscores occurring within the link, in descending order
            String substring = input.substring(marker.start, marker.end);
            StringBuilder link = new StringBuilder(substring);

            List<Integer> underscores = getIndicesOfAllUnderscores(substring);
            if (underscores.size() == 0) {
                // Links without underscores don't affect formatting, so ignore them
                continue;
            }

            // In both the local link and full text, remove the underscores
            for (int i = 0; i < underscores.size(); i++) {
                int index = underscores.get(i);
                link.deleteCharAt(index);
                text.deleteCharAt(marker.start + index);
            }

            // Add the cleaned link string to our link map, with its list of underscores
            linkMap.put(link.toString(), underscores);
        }

        // Copy changes to the passed StringBuilder so the changes are synced
        // TODO This is somewhat bad style, we should try to refactor
        input.delete(0, input.length());
        input.append(text);

        return linkMap;
    }

    List<LinkMarker> getMarkersForPattern(final CharSequence text, final Pattern pattern) {
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

    List<LinkMarker> cleanListOfMarkers(final List<LinkMarker> links) {
        List<LinkMarker> clean = new ArrayList<>(links);

        List<Integer> indicesToDelete = new ArrayList<>();

        // FIXME
        // This currently runs in o(n^2), see if we can optimize it somehow so we don't cause
        // bottlenecks in parsing markdown with a lot of URLSpans
        for (int i = 0; i < clean.size(); i++) {
            if (indicesToDelete.contains(i)) {
                continue;
            }

            LinkMarker marker = clean.get(i);

//            Timber.d("SPAN(%d - %d)", marker.start, marker.end);

            // Look ahead to spans that start before the end of this span, and remove them
            for (int j = 0; j < clean.size(); j++) {
                if (indicesToDelete.contains(j)) {
                    continue;
                }

                LinkMarker nextMarker = clean.get(j);
                // If we're at the same span, move onto the next one
                if (marker == nextMarker) {
                    continue;
                }

                // Check if this span starts before the last one ends
                if (nextMarker.start >= marker.start && nextMarker.start <= marker.end) {
//                    Timber.d("This span starts and ends within the current span, so remove it");
                    indicesToDelete.add(j);
                }
            }
        }

        // Remove identified indices for deletion
        Collections.sort(indicesToDelete);
        for (int i = indicesToDelete.size() - 1; i >= 0; i--) {
            int index = indicesToDelete.get(i);
            clean.remove(index);
        }

        return clean;
    }

    List<Integer> getIndicesOfAllUnderscores(String text) {
        List<Integer> indices = new ArrayList<>();
        for (int i = text.length() - 1; i >= 0; i--) {
            if (text.charAt(i) == '_') {
                indices.add(i);
            }
        }
        return indices;
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
//            Timber.d("SPAN(%d - %d)", spanStart, spanEnd);

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
//                    Timber.d("This span starts and ends within the current span, so remove it");
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

                int startIndex = url.indexOf(linkText);
                int endIndex = startIndex + linkText.length() - 1;

                url.deleteCharAt(endIndex);
                url.deleteCharAt(startIndex);

                text.removeSpan(urlSpan);
                text.setSpan(new URLSpan(url.toString()), spanStart + 1, spanEnd - 1, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    void restoreUnderscoresToText(SpannableStringBuilder text, Map<String, List<Integer>> linkMap) {
        Set<String> links = linkMap.keySet();

        for (String link : links) {
            // Search for instances of the link within the text
            int currentIndex = text.toString().indexOf(link);
            while (currentIndex != -1) {
                // Get the list of underscore indices
                List<Integer> underscores = linkMap.get(link);

                // Get the URLSpan starting at the index and ending at the end of the link
                URLSpan[] spans = text.getSpans(currentIndex, currentIndex + link.length(), URLSpan.class);
                URLSpan span = spans[0];
                StringBuilder url = new StringBuilder(span.getURL());

                StringBuilder newLinkText = new StringBuilder(link);

                // Add underscores to the text and URL at specified indices
                int linkUrlOffset = url.indexOf(link);
                for (int i = underscores.size() - 1; i >= 0; i--) {
                    int index = underscores.get(i);
                    newLinkText.insert(index, "_");
                    url.insert(linkUrlOffset + index, "_");
                }

                // Replace link within full text with the updated link text
                text.replace(currentIndex, currentIndex + link.length(), newLinkText);

                // Replace span with one for the corrected URL
                text.removeSpan(span);
                URLSpan newSpan = new NoUnderlineURLSpan(url.toString());
                text.setSpan(newSpan, currentIndex, currentIndex + newLinkText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Find next instance
                currentIndex = text.toString().indexOf(link, currentIndex + 1);
            }
        }
    }

    static class LinkMarker {
        public int start;
        public int end;

        public LinkMarker(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
