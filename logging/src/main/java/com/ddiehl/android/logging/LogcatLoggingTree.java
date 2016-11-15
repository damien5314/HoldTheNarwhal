package com.ddiehl.android.logging;

import android.util.Log;

import timber.log.Timber;

/**
 * {@link Timber.DebugTree} that forwards all logging calls to android.util.Log (logcat).
 * <p>
 * Also rethrows exceptions above WARN priority, only install in debug builds.
 */
public class LogcatLoggingTree extends Timber.DebugTree {

    private LogcatLogger mLogcatLogger;

    public LogcatLoggingTree(LogcatLogger logger) {
        mLogcatLogger = logger;
    }

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        mLogcatLogger.println(priority, tag, message);

        // Crash above Log.WARN priority
        if (t != null && priority > Log.WARN) {
            throw new RuntimeException(t);
        }
    }
}
