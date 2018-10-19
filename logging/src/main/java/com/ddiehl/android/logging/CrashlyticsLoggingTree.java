package com.ddiehl.android.logging;

import android.util.Log;

import androidx.annotation.NonNull;
import timber.log.Timber;

/**
 * {@link Timber.Tree} that forwards all logging calls to Crashlytics.log(),
 * and exceptions to Crashlytics.logException(), of INFO priority and above.
 * <p>
 * Also sanitizes logging messages that contain a user access token.
 */
public class CrashlyticsLoggingTree extends Timber.Tree {

    private com.ddiehl.android.logging.CrashlyticsLogger crashlyticsLogger;

    public CrashlyticsLoggingTree(com.ddiehl.android.logging.CrashlyticsLogger crashlyticsLogger) {
        this.crashlyticsLogger = crashlyticsLogger;
    }

    @Override
    protected void log(int priority, String tag, @NonNull String message, Throwable t) {
        if (t != null) {
            crashlyticsLogger.logException(t);
        } else {
            // Don't use {@link Crashlytics#log(int, String, String)} - This also prints to logcat
            crashlyticsLogger.log(message);
        }
    }

    @Override
    protected boolean isLoggable(String tag, int priority) {
        // Only send logs to Crashlytics of INFO priority or higher
        return priority >= Log.INFO;
    }
}
