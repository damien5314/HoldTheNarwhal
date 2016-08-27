package com.ddiehl.android.htn.subscriptions;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.activities.BaseActivity;

public class SubscriptionManagerActivity extends BaseActivity {

  public static Intent getIntent(Context context) {
    return new Intent(context, SubscriptionManagerActivity.class);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    showFragment();
  }

  private void showFragment() {
    if (getSupportFragmentManager().findFragmentByTag(SubscriptionManagerFragment.TAG) == null) {
      getSupportFragmentManager().beginTransaction()
          .add(R.id.fragment_container, getFragment(), SubscriptionManagerFragment.TAG)
          .commit();
    }
  }

  Fragment getFragment() {
    return SubscriptionManagerFragment.newInstance();
  }
}
