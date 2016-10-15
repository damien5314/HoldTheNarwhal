package com.ddiehl.android.htn.view.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.ddiehl.android.htn.view.fragments.WebViewFragment;
import com.ddiehl.android.htn.view.fragments.WebViewFragmentBuilder;

public class WebViewActivity extends FragmentActivityCompat {

    public static final String EXTRA_URL = "EXTRA_URL";

    public static Intent getIntent(Context context, String url) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(EXTRA_URL, url);
        return intent;
    }

    @Override
    Fragment getFragment() {
        return new WebViewFragmentBuilder(getUrl())
                .build();
    }

    @Override
    String getFragmentTag() {
        return WebViewFragment.TAG;
    }

    private String getUrl() {
        return getIntent().getStringExtra(EXTRA_URL);
    }
}
