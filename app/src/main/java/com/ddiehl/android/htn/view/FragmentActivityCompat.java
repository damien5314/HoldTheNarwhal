package com.ddiehl.android.htn.view;

import android.support.v4.app.Fragment;

import com.ddiehl.android.htn.R;

import org.jetbrains.annotations.NotNull;

public abstract class FragmentActivityCompat extends BaseActivity {

    protected abstract @NotNull Fragment getFragment();

    protected abstract @NotNull String getFragmentTag();

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
