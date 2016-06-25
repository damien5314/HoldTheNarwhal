package com.ddiehl.android.htn.view.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.ddiehl.android.htn.view.fragments.LinkCommentsFragment;
import com.ddiehl.android.htn.view.fragments.LinkCommentsFragmentBuilder;

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
  Fragment getFragment() {
    return new LinkCommentsFragmentBuilder(getArticleId(), getCommentId(), getSubreddit())
        .build();
  }

  @Override
  String getFragmentTag() {
    return LinkCommentsFragment.TAG;
  }

  public String getSubreddit() {
    return getIntent().getStringExtra(EXTRA_SUBREDDIT);
  }

  public String getArticleId() {
    return getIntent().getStringExtra(EXTRA_ARTICLE_ID);
  }

  public String getCommentId() {
    return getIntent().getStringExtra(EXTRA_COMMENT_ID);
  }
}
