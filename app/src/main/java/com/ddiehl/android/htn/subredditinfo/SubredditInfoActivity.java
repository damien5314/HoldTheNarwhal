package com.ddiehl.android.htn.subredditinfo;

import android.os.Bundle;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.utils.ThemeUtilsKt;
import com.ddiehl.android.htn.view.BaseActivity;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;


public class SubredditInfoActivity extends BaseActivity {

    public static final String EXTRA_SUBREDDIT = "EXTRA_SUBREDDIT";

    String subreddit;

    @Override
    protected boolean hasNavigationDrawer() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getIntent().getExtras().containsKey(EXTRA_SUBREDDIT)) {
            throw new RuntimeException("Activity is missing required extras");
        }

        showTabs(false);

        // Set window background color
        int bgColor = ThemeUtilsKt.getColorFromAttr(this, R.attr.windowBackgroundColorNeutral);
        getWindow().getDecorView().setBackgroundColor(bgColor);

        subreddit = getIntent().getExtras().getString(EXTRA_SUBREDDIT);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(null);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        showInfoFragment();
    }

    void showInfoFragment() {
        if (getSupportFragmentManager().findFragmentByTag(SubredditInfoFragment.TAG) == null) {
            Fragment fragment = new SubredditInfoFragmentBuilder(subreddit)
                    .build();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment, SubredditInfoFragment.TAG)
                    .commit();
        }
    }
}
