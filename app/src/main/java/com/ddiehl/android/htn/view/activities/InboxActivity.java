package com.ddiehl.android.htn.view.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.ddiehl.android.htn.view.fragments.InboxFragment;
import com.ddiehl.android.htn.view.fragments.InboxFragmentBuilder;

public class InboxActivity extends FragmentActivityCompat {

    private static final String EXTRA_SHOW = "EXTRA_SHOW";

    public static Intent getIntent(Context context, String show) {
        Intent intent = new Intent(context, InboxActivity.class);
        intent.putExtra(EXTRA_SHOW, show);
        return intent;
    }

    @Override
    protected boolean hasNavigationDrawer() {
        return true;
    }

    @Override
    Fragment getFragment() {
        return new InboxFragmentBuilder(getShow())
                .build();
    }

    @Override
    String getFragmentTag() {
        return InboxFragment.TAG;
    }

    private String getShow() {
        return getIntent().getStringExtra(EXTRA_SHOW);
    }
}
