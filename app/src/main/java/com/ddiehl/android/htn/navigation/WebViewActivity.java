package com.ddiehl.android.htn.navigation;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.BaseActivity;

import org.jetbrains.annotations.NotNull;

public class WebViewActivity extends BaseActivity {

    public static final String EXTRA_URL = "EXTRA_URL";

    public static Intent getIntent(Context context, String url) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(EXTRA_URL, url);
        return intent;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getSupportFragmentManager().findFragmentByTag(getFragmentTag()) == null) {
            final Fragment fragment = getFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment, getFragmentTag())
                    .commit();
        }
    }

    @Override
    protected boolean hasNavigationDrawer() {
        return false;
    }

    @NotNull
    private Fragment getFragment() {
        return new WebViewFragmentBuilder(getUrl())
                .build();
    }

    @NotNull
    private String getFragmentTag() {
        return WebViewFragment.TAG;
    }

    private String getUrl() {
        return getIntent().getStringExtra(EXTRA_URL);
    }
}
