package com.ddiehl.android.simpleredditreader.view;

import android.support.v4.app.Fragment;


public class FrontPageActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new FrontPageFragment();
    }
}
