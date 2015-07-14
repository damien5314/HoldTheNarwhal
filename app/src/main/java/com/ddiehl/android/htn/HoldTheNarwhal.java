/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;


public class HoldTheNarwhal extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }
}
