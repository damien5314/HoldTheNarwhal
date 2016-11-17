package com.ddiehl.android.htn.view;

import android.support.v4.app.Fragment;

import com.ddiehl.android.htn.R;

public abstract class FragmentActivityCompat extends BaseActivity {

    protected abstract Fragment getFragment();

    protected abstract String getFragmentTag();

    @Override
    public void onStart() {
        super.onStart();
        if (getSupportFragmentManager().findFragmentByTag(getFragmentTag()) == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, getFragment(), getFragmentTag())
                    .commit();
        }
    }
}
