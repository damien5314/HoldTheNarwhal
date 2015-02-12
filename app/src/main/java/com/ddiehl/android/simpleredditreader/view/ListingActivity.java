package com.ddiehl.android.simpleredditreader.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.ddiehl.android.simpleredditreader.R;


public class ListingActivity extends SingleFragmentActivity {
    private static final String TAG = ListingActivity.class.getSimpleName();

    public static final String EXTRA_SUBREDDIT = "com.ddiehl.android.simpleredditreader.extra_subreddit";

    private String mSubreddit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle extras = getIntent().getExtras();
        if (extras != null
                && extras.containsKey(EXTRA_SUBREDDIT)) {
            mSubreddit = extras.getString(EXTRA_SUBREDDIT);
        }

        // If started as the launcher activity, default to /r/all
        if (mSubreddit == null) {
            mSubreddit = getString(R.string.default_subreddit);
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    protected Fragment createFragment() {
        return ListingFragment.newInstance(mSubreddit);
    }
}
