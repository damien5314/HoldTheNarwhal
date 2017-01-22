package com.ddiehl.android.htn.about;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.ddiehl.android.htn.view.FragmentActivityCompat;

import timber.log.Timber;

public class AboutAppActivity extends FragmentActivityCompat {

    public static Intent getIntent(Context context) {
        return new Intent(context, AboutAppActivity.class);
    }

    @Override
    protected boolean hasNavigationDrawer() {
        return false;
    }

    @NonNull @Override
    protected Fragment getFragment() {
        return AboutAppFragment.newInstance();
    }

    @NonNull @Override
    protected String getFragmentTag() {
        return AboutAppFragment.TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.i("Showing about app");
        showTabs(false);
    }
}
