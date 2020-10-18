package com.ddiehl.android.logging;


import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class CrashlyticsLogger {

    public void log(String message) {
        FirebaseCrashlytics.getInstance().log(message);
    }

    public void logException(Throwable t) {
        FirebaseCrashlytics.getInstance().recordException(t);
    }
}
