package com.ddiehl.android.htn.di;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module
public class TestApplicationModule {

    Context mContext;

    public TestApplicationModule(Context context) {
        mContext = context;
    }

    @Provides
    Context providesContext() {
        return mContext;
    }
}
