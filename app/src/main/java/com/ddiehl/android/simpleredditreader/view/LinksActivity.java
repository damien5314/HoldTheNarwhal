package com.ddiehl.android.simpleredditreader.view;

import android.os.Bundle;

public class LinksActivity extends NavigationDrawerActivity {
    private static final String TAG = LinksActivity.class.getSimpleName();

    public static final String EXTRA_SUBREDDIT = "com.ddiehl.android.simpleredditreader.extra_subreddit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String subreddit = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            subreddit = extras.getString(EXTRA_SUBREDDIT);
        }

//        Fragment fragment = LinksFragment.newInstance(subreddit);
//        displayFragment(fragment);
    }
}
