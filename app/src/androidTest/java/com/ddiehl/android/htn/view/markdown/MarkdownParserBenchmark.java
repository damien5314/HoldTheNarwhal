package com.ddiehl.android.htn.view.markdown;

import android.support.test.runner.AndroidJUnit4;

import com.ddiehl.android.htn.TestUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import in.uncod.android.bypass.Bypass;

import static android.support.test.InstrumentationRegistry.getContext;

/**
 * 01-12 20:02:40.078 MarkdownParserBenchmark: 10000 iterations took 22071 MILLISECONDS
 * 01-12 20:22:44.819 I/TestUtils: MarkdownParser.convert(): 1686 MILLISECONDS
 * 01-12 22:54:46.920 I/TestUtils: MarkdownParser.convert(): 2163 MILLISECONDS
 */
@RunWith(AndroidJUnit4.class)
public class MarkdownParserBenchmark {

    static MarkdownParser getParser() {
        Bypass bypass = new Bypass(getContext());
        return new MarkdownParser(bypass);
    }

    static final String BENCHMARK_TEXT =
            "https://www.reddit.com/r/IAmA/comments/z1c9z/"
                    + "i_am_barack_obama_president_of_the_united_states/";

    @Test
    public void benchmark() {
        MarkdownParser parser = getParser();

        TestUtils.logDuration("MarkdownParser.convert()", () -> {
            int iterations = 10000;

            for (int i = 0; i < iterations; i++) {
                parser.convert(BENCHMARK_TEXT);
            }
        });
    }
}
