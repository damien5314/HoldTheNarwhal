package com.ddiehl.android.simpleredditreader.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.ddiehl.android.simpleredditreader.R;

public class CommentsActivity extends NavigationDrawerActivity {
    private static final String TAG = CommentsActivity.class.getSimpleName();

    public static final String EXTRA_SUBREDDIT = "com.ddiehl.android.simpleredditreader.extra_subreddit";
    public static final String EXTRA_ARTICLE = "com.ddiehl.android.simpleredditreader.extra_article";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        String subreddit = extras.getString(EXTRA_SUBREDDIT);
        String article = extras.getString(EXTRA_ARTICLE);

        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            Fragment defaultFragment = CommentsFragment.newInstance(subreddit, article);
            displayFragment(defaultFragment);
        }
    }
}
