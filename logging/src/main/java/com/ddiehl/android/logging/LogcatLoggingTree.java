package com.ddiehl.android.logging;

import android.util.Log;

import timber.log.Timber;

/**
 * {@link Timber.DebugTree} that forwards all logging calls to android.util.Log (logcat).
 * <p>
 * Also rethrows exceptions above WARN priority, only install in debug builds.
 */
public class LogcatLoggingTree extends Timber.DebugTree {

    private LogcatLogger logcatLogger;

    public LogcatLoggingTree(LogcatLogger logger) {
        logcatLogger = logger;
    }

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        logcatLogger.println(priority, tag, message);

        // Crash above Log.WARN priority
        if (t != null && priority > Log.WARN) {
            throw new RuntimeException(t);
        }
    }
}
