package com.ddiehl.android.htn.view.markdown;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import in.uncod.android.bypass.Bypass;
import timber.log.Timber;

import static android.support.test.InstrumentationRegistry.getContext;

/**
 * 01-12 20:02:40.078 MarkdownParserBenchmark: 10000 iterations took 22071 MILLISECONDS
 * 01-12 20:05:22.045 MarkdownParserBenchmark: 10000 iterations took 1672 MILLISECONDS
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

        int iterations = 10000;

        long startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            parser.convert(BENCHMARK_TEXT);
        }

        long endTime = System.nanoTime();

        long elapsed = TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);

        Timber.i("%d iterations took %d %s", iterations, elapsed, TimeUnit.MILLISECONDS.name());
    }
}
