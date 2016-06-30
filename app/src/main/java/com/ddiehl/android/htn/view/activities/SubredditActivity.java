package com.ddiehl.android.htn.view.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.ddiehl.android.htn.view.fragments.SubredditFragment;
import com.ddiehl.android.htn.view.fragments.SubredditFragmentBuilder;

public class SubredditActivity extends FragmentActivityCompat {

  public static final String TAG = SubredditActivity.class.getSimpleName();

  private static final String EXTRA_SUBREDDIT = "EXTRA_SUBREDDIT";
  private static final String EXTRA_SORT = "EXTRA_SORT";
  private static final String EXTRA_TIMESPAN = "EXTRA_TIMESPAN";

  public static Intent getIntent(Context context, String subreddit, String sort, String timespan) {
    Intent intent = new Intent(context, SubredditActivity.class);
    intent.putExtra(EXTRA_SUBREDDIT, subreddit);
    intent.putExtra(EXTRA_SORT, sort);
    intent.putExtra(EXTRA_TIMESPAN, timespan);
    return intent;
  }

  @Override
  Fragment getFragment() {
    return new SubredditFragmentBuilder()
        .subreddit(getSubreddit())
        .sort(getSort())
        .timespan(getTimespan())
        .build();
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

  private String getTimespan() {
    return getIntent().getStringExtra(EXTRA_TIMESPAN);
  }
}
