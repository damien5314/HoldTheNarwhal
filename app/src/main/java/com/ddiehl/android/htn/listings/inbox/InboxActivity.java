package com.ddiehl.android.htn.listings.inbox;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.ddiehl.android.htn.view.FragmentActivityCompat;

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
    protected Fragment getFragment() {
        return new InboxFragmentBuilder(getShow())
                .build();
    }

    @Override
    protected String getFragmentTag() {
        return InboxFragment.TAG;
    }

    private String getShow() {
        return getIntent().getStringExtra(EXTRA_SHOW);
    }
}
