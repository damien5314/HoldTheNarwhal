package com.ddiehl.android.htn;

import android.content.Context;

public class AndroidContextProvider {
    private static Context mContext;

    public static void setContext(Context context) {
        mContext = context.getApplicationContext();
    }

    public static Context getContext() {
        return mContext;
    }
}
