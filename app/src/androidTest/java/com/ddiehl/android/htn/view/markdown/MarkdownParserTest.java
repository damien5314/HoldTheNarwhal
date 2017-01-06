package com.ddiehl.android.htn.view.markdown;

import android.support.test.runner.AndroidJUnit4;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;

import com.ddiehl.android.logging.ConsoleLogger;
import com.ddiehl.android.logging.ConsoleLoggingTree;

import org.junit.Before;
import org.junit.Ignore;
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
    @Ignore("Using Patterns.WEB_URL actually links strings with only a host, which is fine")
    public void convert_hostOnly_hasNoFormatting() {
        MarkdownParser parser = getParser();
        String url = "reddit.com/wiki/index";

        CharSequence formatted = parser.convert(url);
        SpannableString result = SpannableString.valueOf(formatted);

        Object[] spans = result.getSpans(0, result.length(), Object.class);
        assertEquals(0, spans.length);
    }

    @Test
    public void convert_hostOnlyWithSubredditLink_hasOnlySubredditLinkFormatted() {
        MarkdownParser parser = getParser();
        String url = "reddit.com/r/Android/comments/3vjmcx/";

        CharSequence formatted = parser.convert(url);
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(1, urlSpans.length);

        URLSpan span = urlSpans[0];
        assertEquals("https://" + url, span.getURL());
        // Using Patterns.WEB_URL will actually link the whole string,
        // even though this isn't the behavior on reddit itself
//        assertEquals(url.indexOf("/r/"), result.getSpanStart(span));
        assertEquals(0, result.getSpanStart(span));
    }

    @Test
    public void convert_urlWithUnderscores_hasNoInnerFormatting() throws Exception {
        MarkdownParser parser = getParser();

        CharSequence formatted = parser.convert(
                "https://www.reddit.com/r/Android/comments/3vjmcx/google_play_music_is_simply_marvellous/"
        );
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
    public void convert_urlEndingWithParens_hasUrlSpan() {
        MarkdownParser parser = getParser();
        String url = "https://en.wikipedia.org/wiki/Shake_Hands_with_the_Devil_(book)";
        // FIXME
        // For some reason, the Patterns.WEB_URL regex doesn't capture the ending parenthesis
        // only when the URL has newlines before it.
        String text = url + "\n\n" + url;

        CharSequence formatted = parser.convert(text);
        SpannableString result = SpannableString.valueOf(formatted);

        URLSpan[] urlSpans = result.getSpans(0, result.length(), URLSpan.class);
        assertEquals(2, urlSpans.length);

        for (int i = urlSpans.length - 1; i >= 0; i--) {
            URLSpan span = urlSpans[i];
            assertEquals(url, span.getURL());
        }
    }

    // TODO
    // Fix the below method first, the above is probably broken for the same reason (subdomains
    // aren't recognized as valid URLs) but that one will also have another bug once this is fixed.

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
}
