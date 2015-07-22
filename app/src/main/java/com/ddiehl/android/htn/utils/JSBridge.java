/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.webkit.JavascriptInterface;

import java.text.SimpleDateFormat;
import java.util.Date;

public class JSBridge {

    private Context mContext;

    public JSBridge(Context c) {
        mContext = c.getApplicationContext();
    }

    @JavascriptInterface
    public String getVersionName() {
        try {
            String packageName = mContext.getPackageName();
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(packageName, 0);
            return pInfo.versionName;
        } catch (Exception e) {
            return null;
        }
    }

    @JavascriptInterface
    public String getUpdated() {
        try {
            String packageName = mContext.getPackageName();
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(packageName, 0);

            return SimpleDateFormat.getDateInstance().format(new Date(pInfo.lastUpdateTime));
        } catch (Exception e) {
            return null;
        }
    }

}
