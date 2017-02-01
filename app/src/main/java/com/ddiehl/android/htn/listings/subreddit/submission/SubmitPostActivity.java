package com.ddiehl.android.htn.listings.subreddit.submission;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.FragmentActivityCompat;


public class SubmitPostActivity extends FragmentActivityCompat {

    public static final String TAG = SubmitPostActivity.class.getSimpleName();

    public static final String EXTRA_SUBREDDIT = "EXTRA_SUBREDDIT";

    public static Intent getIntent(@NonNull Context context, @NonNull String subreddit) {
        Intent intent = new Intent(context, SubmitPostActivity.class);
        intent.putExtra(EXTRA_SUBREDDIT, subreddit);
        return intent;
    }

    String mSubreddit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        mSubreddit = extras.getString(EXTRA_SUBREDDIT);

        String title = getString(R.string.submission_activity_title, mSubreddit);
        setTitle(title);
    }

    @Override
    protected boolean hasNavigationDrawer() {
        return false;
    }

    @NonNull
    @Override
    protected Fragment getFragment() {
        return new SubmitPostFragmentBuilder(mSubreddit)
                .build();
    }

    @NonNull
    @Override
    protected String getFragmentTag() {
        return SubmitPostFragment.TAG;
    }
}
