package com.ddiehl.android.htn.listings.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ddiehl.android.htn.view.FragmentActivityCompat;

import org.jetbrains.annotations.NotNull;

import androidx.fragment.app.Fragment;

public class UserProfileActivity extends FragmentActivityCompat {

    private static final String EXTRA_USERNAME = "EXTRA_USERNAME";
    private static final String EXTRA_SHOW = "EXTRA_SHOW";
    private static final String EXTRA_SORT = "EXTRA_SORT";
    private static final String EXTRA_TIMESPAN = "EXTRA_TIMESPAN";

    public static Intent getIntent(Context context, String username, String show, String sort) {
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtra(EXTRA_USERNAME, username);
        intent.putExtra(EXTRA_SHOW, show);
        intent.putExtra(EXTRA_SORT, sort);
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
        return new UserProfileFragmentBuilder(getShow(), getSort(), getTimespan(), getUsername())
                .build();
    }

    @NotNull @Override
    protected String getFragmentTag() {
        return UserProfileFragment.TAG;
    }

    public String getUsername() {
        return getIntent().getStringExtra(EXTRA_USERNAME);
    }

    public String getShow() {
        return getIntent().getStringExtra(EXTRA_SHOW);
    }

    public String getSort() {
        return getIntent().getStringExtra(EXTRA_SORT);
    }

    public String getTimespan() {
        return getIntent().getStringExtra(EXTRA_TIMESPAN);
    }
}
