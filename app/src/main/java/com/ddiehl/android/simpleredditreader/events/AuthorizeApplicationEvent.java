package com.ddiehl.android.simpleredditreader.events;

import android.content.Context;

public class AuthorizeApplicationEvent {
    private Context mContext;

    public AuthorizeApplicationEvent(Context c) {
        mContext = c;
    }

    public Context getContext() {
        return mContext;
    }
}
