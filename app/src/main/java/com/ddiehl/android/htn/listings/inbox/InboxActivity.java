package com.ddiehl.android.htn.listings.inbox;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.BaseActivity;

public class InboxActivity extends BaseActivity {

    private static final String EXTRA_SHOW = "EXTRA_SHOW";

    public static Intent getIntent(Context context, String show) {
        Intent intent = new Intent(context, InboxActivity.class);
        intent.putExtra(EXTRA_SHOW, show);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showTabs(true);
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
        return true;
    }

    private Fragment getFragment() {
        return new InboxFragmentBuilder(getShow())
                .build();
    }

    private String getFragmentTag() {
        return InboxFragment.TAG;
    }

    private String getShow() {
        return getIntent().getStringExtra(EXTRA_SHOW);
    }
}
