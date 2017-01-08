package com.ddiehl.android.htn.view.markdown;

import android.support.test.runner.AndroidJUnit4;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;

import com.ddiehl.android.logging.ConsoleLogger;
import com.ddiehl.android.logging.ConsoleLoggingTree;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import in.uncod.android.bypass.Bypass;
import timber.log.Timber;

import static android.support.test.InstrumentationRegistry.getContext;
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
}
