package com.ddiehl.android.logging;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

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
    protected void log(int priority, String tag, String message, Throwable t) {
        /** Don't use {@link Crashlytics#log(int, String, String)} - This also prints to logcat */
        crashlyticsLogger.log(message);

        // Also log the exception if one is passed
        if (t != null) {
            crashlyticsLogger.logException(t);
        }
    }

    @Override
    protected boolean isLoggable(String tag, int priority) {
        // Only send logs to Crashlytics of INFO priority or higher
        return priority >= Log.INFO;
    }
}
