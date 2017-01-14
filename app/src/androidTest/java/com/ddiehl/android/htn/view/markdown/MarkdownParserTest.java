package com.ddiehl.android.htn.view.markdown;

import android.graphics.Typeface;
import android.support.test.runner.AndroidJUnit4;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;

import com.ddiehl.android.logging.ConsoleLogger;
import com.ddiehl.android.logging.ConsoleLoggingTree;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.uncod.android.bypass.Bypass;
import timber.log.Timber;

import static android.support.test.InstrumentationRegistry.getContext;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class MarkdownParserTest {

    static MarkdownParser getParser() {
        Bypass bypass = new Bypass(getContext());
        return new MarkdownParser(bypass);
    }

    @Before
    public void setUp() {
//        TestApplicationComponent component = DaggerTestApplicationComponent.builder()
//                .testApplicationModule(new TestApplicationModule(getContext()))
//                .sharedModule(new SharedModule())
//                .build();
//        HoldTheNarwhal.setTestComponent(component);
//        component.inject(this);

        Timber.plant(new ConsoleLoggingTree(new ConsoleLogger()));
    }

    @Test
    public void init_markdownParser_isNotNull() {
        assertNotNull(getParser());
    }

    @Test
    public void convert_httpLink_hasUrlSpan() {
        MarkdownParser parser = getParser();
        String url = "http://www.reddit.com/";

        CharSequence formatted = parser.convert(url);
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(1, urlSpans.length);

        URLSpan span = urlSpans[0];
        assertEquals(url, span.getURL());
    }

    @Test
    public void convert_httpsLink_hasUrlSpan() {
        MarkdownParser parser = getParser();
        String url = "https://www.reddit.com/";

        CharSequence formatted = parser.convert(url);
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(1, urlSpans.length);

        URLSpan span = urlSpans[0];
        assertEquals(url, span.getURL());
    }

    @Test
    public void convert_wwwLink_hasUrlSpan() {
        MarkdownParser parser = getParser();
        String url = "www.reddit.com";

        CharSequence formatted = parser.convert(url);
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(1, urlSpans.length);

        URLSpan span = urlSpans[0];
        assertEquals("https://" + url, span.getURL());
        assertEquals(url, formatted.toString());
    }

    @Test
    public void convert_linkWithProtocolAndSubredditPath_hasUrlSpan() {
        MarkdownParser parser = getParser();
        String url = "https://www.reddit.com/r/Android/";

        CharSequence formatted = parser.convert(url);
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(1, urlSpans.length);

        URLSpan span = urlSpans[0];
        assertEquals(url, span.getURL());
    }

    @Test
    public void convert_hostOnly_hasUrlSpan() {
        MarkdownParser parser = getParser();
        String url = "reddit.com/wiki/index";

        CharSequence formatted = parser.convert(url);
        SpannableString result = SpannableString.valueOf(formatted);

        Object[] spans = result.getSpans(0, result.length(), Object.class);
        assertEquals(0, spans.length);
    }

    @Test
    public void convert_hostOnlyWithSubredditLink_hasUrlSpan() {
        MarkdownParser parser = getParser();
        String subredditPath = "/r/Android/comments/3vjmcx/google_play_music_is_simply_marvellous/";
        String url = "reddit.com" + subredditPath;

        CharSequence formatted = parser.convert(url);
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(1, urlSpans.length);

        URLSpan span = urlSpans[0];
        assertEquals(url.indexOf(subredditPath), result.getSpanStart(span));
        assertEquals(url.length(), result.getSpanEnd(span));
//        assertEquals("https://" + url, span.getURL());
//        assertEquals(0, result.getSpanStart(span));
    }

    @Test
    public void convert_urlWithUnderscores_hasNoInnerFormatting() throws Exception {
        MarkdownParser parser = getParser();
        String url = "https://www.reddit.com/r/Android/comments/3vjmcx/google_play_music_is_simply_marvellous/";

        CharSequence formatted = parser.convert(url);
        SpannableString result = SpannableString.valueOf(formatted);

        StyleSpan[] styleSpans = result.getSpans(0, result.length(), StyleSpan.class);
        assertEquals(0, styleSpans.length);
    }

    @Test
    public void convert_urlWithUnderscores_matchesSpanUrl() throws Exception {
        MarkdownParser parser = getParser();
        String url = "https://www.reddit.com/r/Android/comments/3vjmcx/google_play_music_is_simply_marvellous/";

        CharSequence formatted = parser.convert(url);
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(1, urlSpans.length);

        URLSpan urlSpan = urlSpans[0];
        assertEquals(url, urlSpan.getURL());
    }

    @Test
    public void convert_subredditLink_leadingSlash_hasUrlSpan() {
        MarkdownParser parser = getParser();
        String url = "/r/AndroidDev";

        CharSequence formatted = parser.convert(url);
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(1, urlSpans.length);

        URLSpan span = urlSpans[0];
        assertEquals("https://www.reddit.com" + url, span.getURL());
    }

    @Test
    public void convert_subredditLink_noLeadingSlash_hasUrlSpan() {
        MarkdownParser parser = getParser();
        String url = "r/AndroidDev";

        CharSequence formatted = parser.convert(url);
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(1, urlSpans.length);

        URLSpan span = urlSpans[0];
        assertEquals("https://www.reddit.com/" + url, span.getURL());
    }

    @Test
    public void convert_redditUserLink_leadingSlash_hasUrlSpan() {
        MarkdownParser parser = getParser();
        String url = "/u/andrewsmith1984";

        CharSequence formatted = parser.convert(url);
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(1, urlSpans.length);

        URLSpan span = urlSpans[0];
        assertEquals("https://www.reddit.com" + url, span.getURL());
    }

    @Test
    public void convert_redditUserLink_noLeadingSlash_hasUrlSpan() {
        MarkdownParser parser = getParser();
        String url = "u/andrewsmith1984";

        CharSequence formatted = parser.convert(url);
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(1, urlSpans.length);

        URLSpan span = urlSpans[0];
        assertEquals("https://www.reddit.com/" + url, span.getURL());
    }

    @Test
    public void convert_textWithRSlash_noUrlSpan() {
        MarkdownParser parser = getParser();

        // Text has `r/` but should not be linked
        CharSequence formatted = parser.convert("/user/damien5314");
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(0, urlSpans.length);
    }

    @Test
    public void convert_textWithUSlash_noUrlSpan() {
        MarkdownParser parser = getParser();

        // Text has `u/` but should not be linked
        CharSequence formatted = parser.convert("/fuuu/damien5314");
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(0, urlSpans.length);
    }

    @Test
    public void convert_textStartingWithParens_hasUrlSpan() {
        MarkdownParser parser = getParser();
        String url = "https://www.reddit.com/r/Android/comments/3vjmcx/google_play_music/";
        String text = "(" + url + ")"; // Surround url with parens `()`

        CharSequence formatted = parser.convert(text);
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(1, urlSpans.length);

        URLSpan span = urlSpans[0];
        assertEquals(url, span.getURL());
    }

    @Test
    public void convert_urlWithTrailingParens_hasUrlSpans() {
        MarkdownParser parser = getParser();
        String url = "https://en.wikipedia.org/wiki/Shake_Hands_with_the_Devil_(book)";

        CharSequence formatted = parser.convert(url);
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(1, urlSpans.length);
        assertEquals(url, urlSpans[0].getURL());
    }

    @Test
    public void convert_multilineUrlWithTrailingParens_hasUrlSpans() {
        MarkdownParser parser = getParser();
        String url = "https://en.wikipedia.org/wiki/Shake_Hands_with_the_Devil_(book)";
        String text = url + "\n\n" + url;

        CharSequence formatted = parser.convert(text);
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(2, urlSpans.length);

        for (URLSpan span : urlSpans) {
            assertEquals(url, span.getURL());
        }
    }

    @Test
    public void convert_multilineUrlWithTrailingSlash_hasUrlSpans() {
        MarkdownParser parser = getParser();
        String url = "https://en.wikipedia.org/wiki/";
        String text = url + "\n\n" + url;

        CharSequence formatted = parser.convert(text);
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(2, urlSpans.length);

        for (URLSpan span : urlSpans) {
            assertEquals(url, span.getURL());
        }
    }

    @Test
    public void convert_urlWithNonWwwSubdomain_hasUrlSpan() {
        MarkdownParser parser = getParser();
        String url = "https://help.github.com/";

        CharSequence formatted = parser.convert(url);
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(1, urlSpans.length);

        URLSpan span = urlSpans[0];
        assertEquals(url, span.getURL());
    }

    @Test
    public void convert_urlWithinText_hasUrlSpan() {
        MarkdownParser parser = getParser();
        String url = "https://help.github.com/";
        String text = "This URL (" + url + ") occurs within text";

        CharSequence formatted = parser.convert(text);
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(1, urlSpans.length);

        URLSpan span = urlSpans[0];
        assertEquals(url, span.getURL());
    }

    @Test
    public void convert_textWithMarkdownLinks_doNotReformatLinks() {
        MarkdownParser parser = getParser();
        String linkText = "link to GitHub";
        String url = "https://help.github.com/";
        String text = String.format("this is a [%s](%s)", linkText, url);

        CharSequence formatted = parser.convert(text);
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(1, urlSpans.length);

        URLSpan span = urlSpans[0];
        assertEquals(url, span.getURL());

        String expected = "this is a link to GitHub";
        assertEquals(expected, formatted.toString());
    }

    @Test
    public void convert_subredditLinkWithinText_hasCorrectFormatting() {
        MarkdownParser parser = getParser();
        String subredditLink = "r/cats";
        String text = String.format("Let's check out %s, to see cat photos.", subredditLink);

        CharSequence formatted = parser.convert(text);
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(1, urlSpans.length);

        URLSpan span = urlSpans[0];
        assertEquals("https://www.reddit.com/" + subredditLink, span.getURL());
    }

    @Test
    public void convert_subredditLinkWithinText_withLeadingSlash_hasCorrectFormatting() {
        MarkdownParser parser = getParser();
        String subredditLink = "/r/cats";
        String text = String.format("Let's check out %s, to see cat photos.", subredditLink);

        CharSequence formatted = parser.convert(text);
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(1, urlSpans.length);

        URLSpan span = urlSpans[0];
        assertEquals("https://www.reddit.com" + subredditLink, span.getURL());
    }

    @Test
    public void convert_subredditLinkWithinText_withinParens_hasCorrectFormatting() {
        MarkdownParser parser = getParser();
        String subredditLink = "r/cats";
        String text = String.format("Have you been to that subreddit (%s)?", subredditLink);

        CharSequence formatted = parser.convert(text);
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(1, urlSpans.length);

        URLSpan span = urlSpans[0];
        assertEquals("https://www.reddit.com/" + subredditLink, span.getURL());
    }

    @Test
    public void convert_subredditLinkWithinText_withinParens_withLeadingSlash_hasCorrectFormatting() {
        MarkdownParser parser = getParser();
        String subredditLink = "/r/cats";
        String text = String.format("Have you been to that subreddit (%s)?", subredditLink);

        CharSequence formatted = parser.convert(text);
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(1, urlSpans.length);

        URLSpan span = urlSpans[0];
        assertEquals("https://www.reddit.com" + subredditLink, span.getURL());
    }

    @Test
    public void convert_ellipsisInText_hasNoUrlSpan() {
        MarkdownParser parser = getParser();
        String text = "this text....should not have a link in it";

        CharSequence formatted = parser.convert(text);
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(0, urlSpans.length);
    }

    @Test
    public void convert_boldedRedditLink_leadingSlash_hasCorrectFormatting() {
        testBoldedRedditLinks("/u/FooBar", "https://www.reddit.com/u/FooBar");
    }

    @Test
    public void convert_boldedRedditLink_noLeadingSlash_hasCorrectFormatting() {
        testBoldedRedditLinks("u/FooBar", "https://www.reddit.com/u/FooBar");
    }

    @Test
    public void convert_boldedRedditLink_innerUnderscore_hasCorrectFormatting() {
        testBoldedRedditLinks("/u/Foo_Bar", "https://www.reddit.com/u/Foo_Bar");
    }

    private void testBoldedRedditLinks(String redditLink, String expectedUrl) {
        MarkdownParser parser = getParser();
        String text = String.format("this text **%s** should be stylized", redditLink);

        CharSequence formatted = parser.convert(text);
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(1, urlSpans.length);
        assertEquals("this text " + redditLink + " should be stylized", result.toString());
        assertEquals(expectedUrl, urlSpans[0].getURL());
    }

    /**
     * This is failing because we strip the trailing underscore when we pre-process matching
     * links for underscores, so when we pass it through the markdown parser, there is
     * no underscore to complete the formatting.
     * This may cause issues when users try to italicize any reddit link with underscores.
     */
    @Test @Ignore
    public void convert_italicizedRedditLink_innerUnderscore_hasCorrectFormatting() {
        MarkdownParser parser = getParser();
        String redditLink = "/u/Foo_Bar";
        String text = "_" + redditLink + "_";

        CharSequence formatted = parser.convert(text);
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(1, urlSpans.length);
        assertEquals(redditLink, result.toString());
    }

    /**
     * This is currently failing because we store stripped links in the Map returned from
     * processUnderscoresInLinks so we match both the stripped and original versions
     * when we go to restore the underscores.
     */
    @Test @Ignore
    public void convert_duplicateLinksWithoutUnderscores_areRestoredCorrectly() {
        MarkdownParser parser = getParser();
        String link1 = "/u/FooBar";
        String link2 = "/u/Foo_Bar";
        String text = "**" + link1 + "**" + " " + "**" + link2 + "**";
        String expected = link1 + " " + link2;

        CharSequence formatted = parser.convert(text);
        SpannableString result = SpannableString.valueOf(formatted);

        assertEquals(expected, formatted.toString());
    }

    @Test
    public void getMarkersForPattern() {
        MarkdownParser parser = getParser();
        String link = "https://www.reddit.com/";
        String text = String.format("this is a test with a link %s and some extra text", link);

        List<MarkdownParser.LinkMarker> markers =
                parser.getMarkersForPattern(text, MarkdownParser.URL_PATTERN_WITH_PROTOCOL);

        // Verify only 1 marker was found
        assertEquals(1, markers.size());

        MarkdownParser.LinkMarker marker = markers.get(0);

        // Verify start index and end index are at the correct places
        assertEquals(text.indexOf(link), marker.start);
        assertEquals(text.indexOf(link) + link.length(), marker.end);
    }

    @Test
    public void cleanListOfLinkMarkers_shareStartPoint() {
        MarkdownParser parser = getParser();
        List<MarkdownParser.LinkMarker> markers = new ArrayList<>();
        markers.add(new MarkdownParser.LinkMarker(0, 10));
        markers.add(new MarkdownParser.LinkMarker(0, 9));

        List<MarkdownParser.LinkMarker> clean = parser.cleanListOfMarkers(markers);
        assertEquals(1, clean.size());
        MarkdownParser.LinkMarker marker = clean.get(0);
        assertEquals(0, marker.start);
        assertEquals(10, marker.end);
    }

    @Test
    public void cleanListOfLinkMarkers_shareEndPoint() {
        MarkdownParser parser = getParser();
        List<MarkdownParser.LinkMarker> markers = new ArrayList<>();
        markers.add(new MarkdownParser.LinkMarker(0, 10));
        markers.add(new MarkdownParser.LinkMarker(1, 10));

        List<MarkdownParser.LinkMarker> clean = parser.cleanListOfMarkers(markers);
        assertEquals(1, clean.size());
        MarkdownParser.LinkMarker marker = clean.get(0);
        assertEquals(0, marker.start);
        assertEquals(10, marker.end);
    }

    @Test
    public void cleanListOfLinkMarkers_nested() {
        MarkdownParser parser = getParser();
        List<MarkdownParser.LinkMarker> markers = new ArrayList<>();
        markers.add(new MarkdownParser.LinkMarker(27, 78));
        markers.add(new MarkdownParser.LinkMarker(35, 78));
        markers.add(new MarkdownParser.LinkMarker(49, 78));

        List<MarkdownParser.LinkMarker> clean = parser.cleanListOfMarkers(markers);

        assertEquals(1, clean.size());
        MarkdownParser.LinkMarker marker = clean.get(0);
        assertEquals(27, marker.start);
        assertEquals(78, marker.end);
    }

    @Test
    public void getIndicesOfAllUnderscores() {
        MarkdownParser parser = getParser();
        String text = "_abc_abc_abc_abc_";

        List<Integer> indices = parser.getIndicesOfAllUnderscores(text);
        assertEquals(5, indices.size());
        assertEquals(16, (int) indices.get(0));
        assertEquals(12, (int) indices.get(1));
        assertEquals(8, (int) indices.get(2));
        assertEquals(4, (int) indices.get(3));
        assertEquals(0, (int) indices.get(4));
    }

    @Test
    public void processUnderscoresInLinks() {
        MarkdownParser parser = getParser();
        StringBuilder link = new StringBuilder("https://www.google.com/link_with_some_underscores");
        String stripped = "https://www.google.com/linkwithsomeunderscores";

        Map<String, List<Integer>> map = parser.processUnderscoresInLinks(link);

        // Verify only one link is present in the map
        assertEquals(1, map.size());

        // Verify link has indices mapped to it and with the correct count
        List<Integer> indices = map.get(stripped);
        assertNotNull(indices);
        assertEquals(3, indices.size());

        // Verify StringBuilder passed into function is modified to match stripped link
        assertEquals(link.toString(), stripped);
    }

    @Test
    public void restoreUnderscoresToText() {
        MarkdownParser parser = getParser();
        // Create link Spannable with underscores stripped
        String link = "https://www.google.com/link_with_some_underscores";
        String strippedLink = "https://www.google.com/linkwithsomeunderscores";
        SpannableStringBuilder text = new SpannableStringBuilder(strippedLink);
        text.setSpan(new URLSpan(strippedLink), 0, strippedLink.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Create map of links to underscores
        Map<String, List<Integer>> linkMap = new HashMap<>();
        List<Integer> underscores = parser.getIndicesOfAllUnderscores(link);
        linkMap.put(text.toString(), underscores);

        parser.restoreUnderscoresToText(text, linkMap);

        assertEquals(link, text.toString());
    }

    @Test
    public void convert_italicizedText_underscores_hasCorrectFormatting() {
        MarkdownParser parser = getParser();
        String text = "foo";
        String italics = "_" + text + "_";

        CharSequence formatted = parser.convert(italics);
        SpannableString result = SpannableString.valueOf(formatted);

        assertEquals(text, result.toString());

        StyleSpan[] spans = result.getSpans(0, result.length(), StyleSpan.class);
        assertEquals(1, spans.length);
        assertEquals(Typeface.ITALIC, spans[0].getStyle());
    }

    /**
     * This is currently failing because we replace instances of links within the text
     * without checking if they are a substring of another match
     */
    @Test @Ignore
    public void convert_duplicateLinksThatAreSubstringsOfOneAnother_haveCorrectFormatting() {
        MarkdownParser parser = getParser();
        String redditLink = "/r/subreddit/some_link_with_underscores";
        String httpLink = "https://www.reddit.com" + redditLink;
        String text = httpLink + "\n\n" + redditLink;

        CharSequence formatted = parser.convert(text);
        SpannableString result = SpannableString.valueOf(formatted);

        assertEquals(text, formatted.toString());

        URLSpan[] spans = result.getSpans(0, text.length(), URLSpan.class);
        assertEquals(2, spans.length);

        List<URLSpan> spanList = asList(spans);
        Collections.sort(
                spanList, (o1, o2) -> result.getSpanStart(o1) - result.getSpanStart(o2)
        );

        // Verify they have the same URL
        assertEquals(httpLink, spanList.get(0).getURL());
        assertEquals(httpLink, spanList.get(1).getURL());

        // Verify the HTTP link span starts and ends at its text length
        assertEquals(0, result.getSpanStart(spanList.get(0)));
        assertEquals(httpLink.length(), result.getSpanEnd(spanList.get(0)));

        // Verify the reddit link span starts and ends at the correct place
        assertEquals(httpLink.length() + 2, result.getSpanStart(spanList.get(1)));
        assertEquals(result.length(), result.getSpanEnd(spanList.get(1)));
    }

    @Test
    public void processUnderscoresInLinks_linksWithoutUnderscores_removedFromMap() {
        MarkdownParser parser = getParser();

        StringBuilder input = new StringBuilder();
        input.append("https://www.google.com/link1").append("\n\n");
        input.append("https://www.google.com/link1_2_3").append("\n\n");
        input.append("https://www.google.com/link2").append("\n\n");
        input.append("https://www.google.com/link2_3_4");

        Map<String, List<Integer>> linkMap = parser.processUnderscoresInLinks(input);
        assertEquals(2, linkMap.size());
    }
}
