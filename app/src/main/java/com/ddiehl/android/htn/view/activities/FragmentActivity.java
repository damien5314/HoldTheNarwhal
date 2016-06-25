package com.ddiehl.android.htn.view.activities;

import android.app.Fragment;

import com.ddiehl.android.htn.R;

public abstract class FragmentActivity extends BaseActivity {

  abstract Fragment getFragment();

  abstract String getFragmentTag();

  @Override
  public void showFragment() {
    if (getFragmentManager().findFragmentByTag(getFragmentTag()) == null) {
      getFragmentManager().beginTransaction()
          .add(R.id.fragment_container, getFragment(), getFragmentTag())
          .commit();
    }
  }
}
