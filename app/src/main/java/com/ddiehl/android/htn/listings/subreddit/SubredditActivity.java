package com.ddiehl.android.htn.listings.subreddit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.BaseActivity;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

public class SubredditActivity extends BaseActivity {

    public static final String TAG = SubredditActivity.class.getSimpleName();

    private static final String EXTRA_SUBREDDIT = "EXTRA_SUBREDDIT";
    private static final String EXTRA_SORT = "EXTRA_SORT";
    private static final String EXTRA_TIMESPAN = "EXTRA_TIMESPAN";

    public static Intent getIntent(Context context, String subreddit, String sort, String timespan) {
        Intent intent = new Intent(context, SubredditActivity.class);
        intent.putExtra(EXTRA_SUBREDDIT, subreddit);
        intent.putExtra(EXTRA_SORT, sort);
        intent.putExtra(EXTRA_TIMESPAN, timespan);
        return intent;
    }

    @Override
    protected boolean hasNavigationDrawer() {
        return true;
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

    @NotNull
    private Fragment getFragment() {
        return new SubredditFragmentBuilder()
                .subredditName(getSubredditName())
                .sort(getSort())
                .timespan(getTimespan())
                .build();
    }

    @NotNull
    private String getFragmentTag() {
        return SubredditFragment.TAG;
    }

    private String getSubredditName() {
        return getIntent().getStringExtra(EXTRA_SUBREDDIT);
    }

    private String getSort() {
        return getIntent().getStringExtra(EXTRA_SORT);
    }

    private String getTimespan() {
        return getIntent().getStringExtra(EXTRA_TIMESPAN);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.i("Showing subreddit: %s", getSubredditName());
        showTabs(false);
    }
}
