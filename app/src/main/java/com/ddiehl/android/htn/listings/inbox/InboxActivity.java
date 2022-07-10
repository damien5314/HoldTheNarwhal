package com.ddiehl.android.htn.listings.inbox;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.ddiehl.android.htn.view.FragmentActivityCompat2;

import org.jetbrains.annotations.NotNull;

public class InboxActivity extends FragmentActivityCompat2 {

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

    @NotNull @Override
    protected Fragment getFragment() {
        return new InboxFragmentBuilder(getShow())
                .build();
    }

    @NotNull @Override
    protected String getFragmentTag() {
        return InboxFragment.TAG;
    }

    private String getShow() {
        return getIntent().getStringExtra(EXTRA_SHOW);
    }
}
