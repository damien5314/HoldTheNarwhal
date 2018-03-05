package com.ddiehl.android.logging;

import android.util.Log;

import timber.log.Timber;

/**
 * {@link Timber.DebugTree} that forwards all logging calls to System.out.
 * <p>
 * Also rethrows exceptions above WARN priority, only install in unit tests.
 */
public class ConsoleLoggingTree extends Timber.DebugTree {

    com.ddiehl.android.logging.ConsoleLogger consoleLogger;

    public ConsoleLoggingTree(com.ddiehl.android.logging.ConsoleLogger logger) {
        consoleLogger = logger;
    }

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        consoleLogger.log(tag + ": " + message);

        if (t != null) {
            t.printStackTrace();

            // Also rethrow the exception above Log.WARN priority
            if (priority > Log.WARN) {
                throw new RuntimeException(t);
            }
        }
    }
}
