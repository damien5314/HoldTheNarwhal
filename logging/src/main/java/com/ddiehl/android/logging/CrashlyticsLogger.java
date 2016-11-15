package com.ddiehl.android.logging;

import com.crashlytics.android.Crashlytics;

public class CrashlyticsLogger {

    public void log(String message) {
        Crashlytics.log(message);
    }

    public void logException(Throwable t) {
        Crashlytics.logException(t);
    }
}
