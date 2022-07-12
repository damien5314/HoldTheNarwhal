package com.ddiehl.android.htn.listings.subreddit.submission;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.BaseActivity;

import org.jetbrains.annotations.NotNull;


public class SubmitPostActivity extends BaseActivity {

    public static final String TAG = SubmitPostActivity.class.getSimpleName();

    public static final String EXTRA_SUBREDDIT = "EXTRA_SUBREDDIT";

    public static Intent getIntent(@NotNull Context context, @NotNull String subreddit) {
        Intent intent = new Intent(context, SubmitPostActivity.class);
        intent.putExtra(EXTRA_SUBREDDIT, subreddit);
        return intent;
    }

    String subreddit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        subreddit = extras.getString(EXTRA_SUBREDDIT);

        String title = getString(R.string.submission_activity_title, subreddit);
        setTitle(title);
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

    private Fragment getFragment() {
        return new SubmitPostFragmentBuilder(subreddit)
                .build();
    }

    private String getFragmentTag() {
        return SubmitPostFragment.TAG;
    }
}
