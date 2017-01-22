package com.ddiehl.android.htn.listings.subreddit.submission;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.ddiehl.android.htn.view.FragmentActivityCompat;


public class SubmitPostActivity extends FragmentActivityCompat {

    public static final String TAG = SubmitPostActivity.class.getSimpleName();

    @Override protected boolean hasNavigationDrawer() {
        return false;
    }

    @NonNull @Override protected Fragment getFragment() {
        return new SubmitPostFragmentBuilder()
                .build();
    }

    @NonNull @Override protected String getFragmentTag() {
        return SubmitPostFragment.TAG;
    }
}
