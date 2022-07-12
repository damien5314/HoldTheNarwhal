package com.ddiehl.android.htn.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.BaseActivity;

public class SettingsActivity extends BaseActivity {

    public static Intent getIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    protected boolean hasNavigationDrawer() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showTabs(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getFragmentManager().findFragmentByTag(SettingsFragment.TAG) == null) {
            final SettingsFragment fragment = new SettingsFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment, SettingsFragment.TAG)
                    .commit();
        }
    }
}
