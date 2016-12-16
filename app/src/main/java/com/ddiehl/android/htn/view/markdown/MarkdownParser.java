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

public class MarkdownParser {

    Bypass mBypass;

    public MarkdownParser(Bypass bypass) {
        mBypass = bypass;
    }

    public CharSequence convert(String text) {

        Pattern redditLinkMatcher = Pattern.compile(
                "(?:^)/?[ru]/\\S+", Pattern.MULTILINE
        );
        Pattern missingProtocolMatcher = Pattern.compile(
                "((http)s?://)?www\\.[^\\s\\)]*", Pattern.MULTILINE
        );
        Pattern anyProtocolMatcher = Pattern.compile("[a-z]+://[^ \\n]*");

        // Find links in the text and surround them with autolink tags '<>' before processing
//            autolink(sb, redditLinkMatcher);
//            autolink(sb, missingProtocolMatcher);
//            autolink(sb, anyProtocolMatcher);

        CharSequence markdown = mBypass.markdownToSpannable(text.toString());
        SpannableStringBuilder formatted = new SpannableStringBuilder(markdown);

        // Add links for /r/ and /u/ patterns
        Linkify.addLinks(
                formatted, redditLinkMatcher, "https://www.reddit.com", null,
                (match, url) -> {
                    url = url.trim();
                    if (!url.startsWith("/")) {
                        url = "/" + url;
                    }
                    return url;
                }
        );

        // Add links missing protocol
        Linkify.addLinks(
                formatted, missingProtocolMatcher, "http://", null,
                (match, url) -> {
                    return url.trim();
                }
        );

        // Add links with any protocol
//            Linkify.addLinks(s, anyProtocolMatcher, null);

        convertFormattingWithinLinks(formatted);

        return formatted;
    }

    void convertFormattingWithinLinks(SpannableStringBuilder string) {
        URLSpan[] spans = string.getSpans(0, string.length(), URLSpan.class);

        for (URLSpan span : spans) {
            int start = string.getSpanStart(span);
            int end = string.getSpanEnd(span);

            StyleSpan[] innerSpans = string.getSpans(start, end, StyleSpan.class);
            for (StyleSpan innerSpan : innerSpans) {
                int spanStart = string.getSpanStart(innerSpan);
                int spanEnd = string.getSpanEnd(innerSpan);
                string.insert(spanStart, "_");
                string.insert(spanEnd + 1, "_");
                string.removeSpan(innerSpan);
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
