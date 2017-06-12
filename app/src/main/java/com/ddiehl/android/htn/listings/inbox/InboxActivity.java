package com.ddiehl.android.htn.listings.inbox;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showTabs(true);
    }

    @Override
    protected boolean hasNavigationDrawer() {
        return true;
    }

    @NonNull @Override
    protected Fragment getFragment() {
        return new InboxFragmentBuilder(getShow())
                .build();
    }

    @NonNull @Override
    protected String getFragmentTag() {
        return InboxFragment.TAG;
    }

    private String getShow() {
        return getIntent().getStringExtra(EXTRA_SHOW);
    }
}
