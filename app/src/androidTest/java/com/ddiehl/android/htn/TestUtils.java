package com.ddiehl.android.htn;

import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class TestUtils {

    public static void logDuration(@NonNull String tag, @NonNull Runnable runnable) {
        long startTime = System.nanoTime();

        runnable.run();

        long endTime = System.nanoTime();

        TimeUnit unit = TimeUnit.MILLISECONDS;
        long elapsed = unit.convert(endTime - startTime, TimeUnit.NANOSECONDS);

        Timber.i("%s: %d %s", tag, elapsed, unit.name());
    }
}
