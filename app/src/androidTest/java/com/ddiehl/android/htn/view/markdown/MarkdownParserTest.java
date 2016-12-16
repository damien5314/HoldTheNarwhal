package com.ddiehl.android.htn.view.markdown;

import android.support.test.runner.AndroidJUnit4;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import in.uncod.android.bypass.Bypass;

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
        assertEquals(url, span.getURL());
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
        assertEquals(url.indexOf("/r/"), result.getSpanStart(span));
        assertEquals("https://" + url, span.getURL());
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
}
