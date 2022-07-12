package com.ddiehl.android.htn.listings.comments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.BaseActivity;

import timber.log.Timber;

public class LinkCommentsActivity extends BaseActivity {

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
        Timber.i("Showing LinkCommentsActivity: %s", getArticleId());
        showTabs(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getSupportFragmentManager().findFragmentByTag(LinkCommentsFragment.TAG) == null) {
            final LinkCommentsFragment fragment = new LinkCommentsFragmentBuilder(
                    getArticleId(),
                    getCommentId(),
                    getSubreddit()
            )
                    .build();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment, LinkCommentsFragment.TAG)
                    .commit();
        }
    }
}
