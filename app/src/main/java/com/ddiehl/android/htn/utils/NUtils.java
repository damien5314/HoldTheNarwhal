/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.utils;

public class NUtils {

    static {
        System.loadLibrary("app");
    }

    public static native String getFlurryApiKey(boolean debugMode);
    public static native String getMoPubApiKey(boolean debugMode);
    public static native String getRedditClientId();

}
