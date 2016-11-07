package com.ddiehl.android.htn.view.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.ddiehl.android.htn.view.fragments.AboutAppFragment;

public class AboutAppActivity extends FragmentActivityCompat {

    public static Intent getIntent(Context context) {
        return new Intent(context, AboutAppActivity.class);
    }

    @Override
    Fragment getFragment() {
        return AboutAppFragment.newInstance();
    }

    @Override
    String getFragmentTag() {
        return AboutAppFragment.TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showTabs(false);
    }
}
