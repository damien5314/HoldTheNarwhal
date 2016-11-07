package com.ddiehl.android.htn.subredditinfo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.activities.BaseActivity;


public class SubredditInfoActivity extends BaseActivity {

    public static final String EXTRA_SUBREDDIT = "EXTRA_SUBREDDIT";

    String mSubreddit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getIntent().getExtras().containsKey(EXTRA_SUBREDDIT)) {
            throw new RuntimeException("Activity is missing required extras");
        }

        showTabs(false);

        // Set window background color
        int bgColor = ContextCompat.getColor(this, R.color.white);
        getWindow().getDecorView().setBackgroundColor(bgColor);

        mSubreddit = getIntent().getExtras().getString(EXTRA_SUBREDDIT);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(null);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        showInfoFragment();
    }

    void showInfoFragment() {
        Fragment fragment = new SubredditInfoFragmentBuilder(mSubreddit)
                .build();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, SubredditInfoFragment.TAG)
                .commit();
    }
}
