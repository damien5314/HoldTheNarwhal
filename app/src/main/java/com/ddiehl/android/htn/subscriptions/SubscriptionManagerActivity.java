package com.ddiehl.android.htn.subscriptions;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.BaseActivity;

public class SubscriptionManagerActivity extends BaseActivity {

    public static Intent getIntent(Context context) {
        return new Intent(context, SubscriptionManagerActivity.class);
    }

    @Override
    protected boolean hasNavigationDrawer() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set window background color
        int bgColor = ContextCompat.getColor(this, R.color.white);
        getWindow().getDecorView().setBackgroundColor(bgColor);

        showTabs(false);
        showFragment();
    }

    private void showFragment() {
        if (getSupportFragmentManager().findFragmentByTag(SubscriptionManagerFragment.TAG) == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, getFragment(), SubscriptionManagerFragment.TAG)
                    .commit();
        }
    }

    Fragment getFragment() {
        return SubscriptionManagerFragment.newInstance();
    }
}
