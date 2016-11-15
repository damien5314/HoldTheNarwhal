package com.ddiehl.android.logging;

import android.util.Log;

public class LogcatLogger {

    public int println(int priority, String tag, String msg) {
        return Log.println(priority, tag, msg);
    }
}
