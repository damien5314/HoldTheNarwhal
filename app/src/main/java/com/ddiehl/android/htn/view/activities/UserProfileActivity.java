package com.ddiehl.android.htn.view.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.ddiehl.android.htn.view.fragments.UserProfileFragment;
import com.ddiehl.android.htn.view.fragments.UserProfileFragmentBuilder;

public class UserProfileActivity extends BaseActivity {

  private static final String EXTRA_USERNAME = "EXTRA_USERNAME";
  private static final String EXTRA_SHOW = "EXTRA_SHOW";
  private static final String EXTRA_SORT = "EXTRA_SORT";
  private static final String EXTRA_TIMESPAN = "EXTRA_TIMESPAN";

  public static Intent getIntent(Context context, String username, String show, String sort) {
    Intent intent = new Intent(context, UserProfileActivity.class);
    intent.putExtra(EXTRA_USERNAME, username);
    intent.putExtra(EXTRA_SHOW, show);
    intent.putExtra(EXTRA_SORT, sort);
    return intent;
  }

  @Override
  Fragment getFragment() {
    return new UserProfileFragmentBuilder(getShow(), getSort(), getTimespan(), getUsername())
        .build();
  }

  @Override
  String getFragmentTag() {
    return UserProfileFragment.TAG;
  }

  public String getUsername() {
    return getIntent().getStringExtra(EXTRA_USERNAME);
  }

  public String getShow() {
    return getIntent().getStringExtra(EXTRA_SHOW);
  }

  public String getSort() {
    return getIntent().getStringExtra(EXTRA_SORT);
  }

  public String getTimespan() {
    return getIntent().getStringExtra(EXTRA_TIMESPAN);
  }
}
