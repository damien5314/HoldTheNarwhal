package com.ddiehl.android.simpleredditreader.view;

import android.support.v4.app.Fragment;

/**
 * Created by Damien on 2/6/2015.
 */
public class WebViewActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new WebViewFragment();
    }
}
