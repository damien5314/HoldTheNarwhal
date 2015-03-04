package com.ddiehl.android.simpleredditreader.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ddiehl.android.simpleredditreader.AuthorizationState;
import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.RedditAuthorization;
import com.ddiehl.android.simpleredditreader.events.ApplicationAuthorizedEvent;
import com.ddiehl.android.simpleredditreader.events.AuthorizeApplicationEvent;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.web.AccessTokenResponse;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class MainActivity extends ActionBarActivity
        implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String EXTRA_SUBREDDIT = "com.ddiehl.android.simpleredditreader.extra_subreddit";
    public static final int REQUEST_AUTHORIZE = 1000;

    private Bus mBus;
    private RedditAuthorization mRedditAuthorization;

    // Navigation drawer
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private View mLogIn, mUserProfile, mFrontPage, mAllSubreddits, mSubreddits,
            mRandomSubreddit, mNavigationDrawer, mNavToSubredditGo;
    private EditText mNavToSubredditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBus = BusProvider.getInstance();
        mRedditAuthorization = RedditAuthorization.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_drawer);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mLogIn = findViewById(R.id.drawer_log_in);
        mLogIn.setOnClickListener(this);

        mUserProfile = findViewById(R.id.drawer_user_profile);
        mUserProfile.setOnClickListener(this);

        mFrontPage = findViewById(R.id.drawer_front_page);
        mFrontPage.setOnClickListener(this);

        mAllSubreddits = findViewById(R.id.drawer_r_all);
        mAllSubreddits.setOnClickListener(this);

        mSubreddits = findViewById(R.id.drawer_subreddits);
        mSubreddits.setOnClickListener(this);

        mRandomSubreddit = findViewById(R.id.drawer_random_subreddit);
        mRandomSubreddit.setOnClickListener(this);

        mNavToSubredditGo = findViewById(R.id.drawer_navigate_to_subreddit_go);
        mNavToSubredditGo.setOnClickListener(this);

        mNavToSubredditText = (EditText) findViewById(R.id.drawer_navigate_to_subreddit_text);
        mNavToSubredditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) { // Hide keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });
        mNavToSubredditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mNavToSubredditGo.performClick();
                    return true;
                }
                return false;
            }
        });

        // Set onClick to null to intercept click events from background
        mNavigationDrawer = findViewById(R.id.navigation_drawer);
        mNavigationDrawer.setOnClickListener(null);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBus.register(this);

        if (mRedditAuthorization.getAuthorizationState() == AuthorizationState.Unauthorized) {
            // TODO Display waiting overlay

            // Post event to authorize application
            mBus.post(new AuthorizeApplicationEvent(this));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBus.unregister(this);
    }

    @Subscribe
    public void onApplicationAuthorized(ApplicationAuthorizedEvent event) {
        if (!event.isFailed()) {
            AccessTokenResponse response = event.getResponse();
            mRedditAuthorization.saveAccessToken(response);

            // TODO Dismiss waiting overlay

            // Display default fragment
            showSubreddit(null);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.drawer_log_in:
                Intent intent = mRedditAuthorization.getAuthorizationIntent(this);
                startActivityForResult(intent, REQUEST_AUTHORIZE);
                break;
            case R.id.drawer_user_profile:
                break;
            case R.id.drawer_front_page:
                showSubreddit(null);
                break;
            case R.id.drawer_r_all:
                showSubreddit("all");
                break;
            case R.id.drawer_subreddits:
                break;
            case R.id.drawer_random_subreddit:
                showSubreddit("random");
                break;
            case R.id.drawer_navigate_to_subreddit_go:
                String inputSubreddit = mNavToSubredditText.getText().toString();
                if (!inputSubreddit.equals("")) {
                    mNavToSubredditText.setText("");
                    showSubreddit(inputSubreddit);
                }
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    public void showSubreddit(String subreddit) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment currentFragment = fm.findFragmentById(R.id.fragment_container);
        // If the current fragment is a LinksFragment, just update the subreddit
        // Else, swap in a LinksFragment
        if (currentFragment instanceof LinksFragment) {
            ((LinksFragment) currentFragment).updateSubreddit(subreddit);
        } else {
            Fragment newFragment = LinksFragment.newInstance(subreddit);
            fm.beginTransaction()
                    .replace(R.id.fragment_container, newFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_AUTHORIZE:
                AuthorizationState state = mRedditAuthorization.getAuthorizationState();
                boolean authorized = state == AuthorizationState.ApplicationAuthorized
                        || state == AuthorizationState.UserAuthorized;
                if (authorized) {
                    Toast.makeText(this, getString(R.string.toast_authorized), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.toast_not_authorized), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
