package com.ddiehl.android.simpleredditreader.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class CommentsActivity extends NavigationDrawerActivity {
    private static final String TAG = CommentsActivity.class.getSimpleName();

    public static final String EXTRA_SUBREDDIT = "com.ddiehl.android.simpleredditreader.extra_subreddit";
    public static final String EXTRA_ARTICLE = "com.ddiehl.android.simpleredditreader.extra_article";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String subreddit = null, article = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            subreddit = extras.getString(EXTRA_SUBREDDIT);
            article = extras.getString(EXTRA_ARTICLE);
        }

        Fragment fragment = CommentsFragment.newInstance(subreddit, article);
        displayFragment(fragment);
    }
}
