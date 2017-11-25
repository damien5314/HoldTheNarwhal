package com.ddiehl.android.htn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class TestUtils {

    public static void logDuration(@NotNull String tag, @NotNull Runnable runnable) {
        long startTime = System.nanoTime();

        runnable.run();

        long endTime = System.nanoTime();

        TimeUnit unit = TimeUnit.MILLISECONDS;
        long elapsed = unit.convert(endTime - startTime, TimeUnit.NANOSECONDS);

        Timber.i("%s: %d %s", tag, elapsed, unit.name());
    }

    @Nullable
    public static String getTextFromInputStream(@NotNull InputStream in) throws IOException {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();
                while (line != null) {
                    sb.append(line);
                    sb.append("\n");
                    line = br.readLine();
                }
                return sb.toString();
            } finally {
                try {
                    br.close();
                } catch (IOException ignored) {
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
