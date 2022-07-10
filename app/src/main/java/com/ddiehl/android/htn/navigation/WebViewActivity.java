package com.ddiehl.android.htn.navigation;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.ddiehl.android.htn.view.FragmentActivityCompat2;

import org.jetbrains.annotations.NotNull;

public class WebViewActivity extends FragmentActivityCompat2 {

    public static final String EXTRA_URL = "EXTRA_URL";

    public static Intent getIntent(Context context, String url) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(EXTRA_URL, url);
        return intent;
    }

    @Override
    protected boolean hasNavigationDrawer() {
        return false;
    }

    @NotNull @Override
    protected Fragment getFragment() {
        return new WebViewFragmentBuilder(getUrl())
                .build();
    }

    @NotNull @Override
    protected String getFragmentTag() {
        return WebViewFragment.TAG;
    }

    private String getUrl() {
        return getIntent().getStringExtra(EXTRA_URL);
    }
}
