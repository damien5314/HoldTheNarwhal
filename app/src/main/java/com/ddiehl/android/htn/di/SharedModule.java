package com.ddiehl.android.htn.di;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module
public class SharedModule {

    Context mContext;

    public SharedModule(Context context) {
        mContext = context.getApplicationContext();
    }

    @Provides
    Context providesContext() {
        return mContext;
    }
}
