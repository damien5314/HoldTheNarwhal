package com.ddiehl.android.htn.view.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.ddiehl.android.htn.view.fragments.LinkCommentsFragment;
import com.ddiehl.android.htn.view.fragments.LinkCommentsFragmentBuilder;

public class LinkCommentsActivity extends FragmentActivityCompat {

    private static final String EXTRA_SUBREDDIT = "EXTRA_SUBREDDIT";
    private static final String EXTRA_ARTICLE_ID = "EXTRA_ARTICLE_ID";
    private static final String EXTRA_COMMENT_ID = "EXTRA_COMMENT_ID";

    public static Intent getIntent(Context context, String subreddit, String linkId, String commentId) {
        Intent intent = new Intent(context, LinkCommentsActivity.class);
        intent.putExtra(EXTRA_SUBREDDIT, subreddit);
        intent.putExtra(EXTRA_ARTICLE_ID, linkId);
        intent.putExtra(EXTRA_COMMENT_ID, commentId);
        return intent;
    }

    @Override
    protected boolean hasNavigationDrawer() {
        return false;
    }

    @Override
    Fragment getFragment() {
        return new LinkCommentsFragmentBuilder(getArticleId(), getCommentId(), getSubreddit())
                .build();
    }

    @Override
    String getFragmentTag() {
        return LinkCommentsFragment.TAG;
    }

    String getSubreddit() {
        return getIntent().getStringExtra(EXTRA_SUBREDDIT);
    }

    String getArticleId() {
        return getIntent().getStringExtra(EXTRA_ARTICLE_ID);
    }

    String getCommentId() {
        return getIntent().getStringExtra(EXTRA_COMMENT_ID);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showTabs(false);
    }
}
