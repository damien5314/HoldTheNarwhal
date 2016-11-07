package com.ddiehl.android.htn.view.activities;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ddiehl.android.htn.view.fragments.SettingsFragment;

public class SettingsActivity extends FragmentActivity {

    public static Intent getIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    protected boolean hasNavigationDrawer() {
        return false;
    }

    @Override
    Fragment getFragment() {
        return new SettingsFragment();
    }

    @Override
    String getFragmentTag() {
        return SettingsFragment.TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showTabs(false);
    }
}
