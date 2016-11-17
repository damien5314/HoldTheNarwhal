package com.ddiehl.android.htn.view;

import android.app.Fragment;

import com.ddiehl.android.htn.R;

public abstract class FragmentActivity extends BaseActivity {

    protected abstract Fragment getFragment();

    protected abstract String getFragmentTag();

    @Override
    public void onStart() {
        super.onStart();
        if (getFragmentManager().findFragmentByTag(getFragmentTag()) == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, getFragment(), getFragmentTag())
                    .commit();
        }
    }
}
