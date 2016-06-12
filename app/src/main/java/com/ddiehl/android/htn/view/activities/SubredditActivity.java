package com.ddiehl.android.htn.view.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.ddiehl.android.htn.view.fragments.SubredditFragment;

public class SubredditActivity extends BaseActivity {

  public static final String TAG = SubredditActivity.class.getSimpleName();

  private static final String EXTRA_SUBREDDIT = "EXTRA_SUBREDDIT";
  private static final String EXTRA_SORT = "EXTRA_SORT";

  public static Intent getIntent(Context context, String subreddit, String sort) {
    Intent intent = new Intent(context, SubredditActivity.class);
    intent.putExtra(EXTRA_SUBREDDIT, subreddit);
    intent.putExtra(EXTRA_SORT, sort);
    return intent;
  }

  @Override
  Fragment getFragment() {
    String subreddit = getSubreddit();
    String sort = getSort();
    return SubredditFragment.newInstance(subreddit, sort);
  }

  @Override
  String getFragmentTag() {
    return SubredditFragment.TAG;
  }

  private String getSubreddit() {
    return getIntent().getStringExtra(EXTRA_SUBREDDIT);
  }

  private String getSort() {
    return getIntent().getStringExtra(EXTRA_SORT);
  }
}
