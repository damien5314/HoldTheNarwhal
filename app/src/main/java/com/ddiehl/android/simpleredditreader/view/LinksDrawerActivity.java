package com.ddiehl.android.simpleredditreader.view;

import android.content.Context;
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

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.model.Sort;
import com.ddiehl.android.simpleredditreader.model.TimeSpan;

public class LinksDrawerActivity extends ActionBarActivity
        implements View.OnClickListener {
    private static final String TAG = LinksDrawerActivity.class.getSimpleName();

    public static final String EXTRA_SUBREDDIT = "com.ddiehl.android.simpleredditreader.extra_subreddit";

    // Navigation drawer
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private EditText mNavToSubredditText;

//    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_links);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_drawer);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        View vLogIn = findViewById(R.id.drawer_log_in);
        vLogIn.setOnClickListener(this);

        View vUserProfile = findViewById(R.id.drawer_user_profile);
        vUserProfile.setOnClickListener(this);

        View vFrontPage = findViewById(R.id.drawer_front_page);
        vFrontPage.setOnClickListener(this);

        View vRAll = findViewById(R.id.drawer_r_all);
        vRAll.setOnClickListener(this);

        View vSubreddits = findViewById(R.id.drawer_subreddits);
        vSubreddits.setOnClickListener(this);

        View vRandomSubreddit = findViewById(R.id.drawer_random_subreddit);
        vRandomSubreddit.setOnClickListener(this);

        View vNavigationDrawer = findViewById(R.id.navigation_drawer);
        vNavigationDrawer.setOnClickListener(null);

        final View vNavToSubredditGo = findViewById(R.id.drawer_navigate_to_subreddit_go);
        vNavToSubredditGo.setOnClickListener(this);

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
                    vNavToSubredditGo.performClick();
                    return true;
                }
                return false;
            }
        });

        String subreddit = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            subreddit = extras.getString(EXTRA_SUBREDDIT);
        }

        if (savedInstanceState == null) {
            Fragment fragment = LinksFragment.newInstance(subreddit, Sort.HOT, TimeSpan.ALL);
            displayFragment(fragment);
        }

//        mViewPager = (ViewPager) findViewById(R.id.view_pager);
//        mViewPager.setAdapter(new LinkFragmentPagerAdapter(getSupportFragmentManager()));
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
    public void onClick(View v) {
        LinksFragment fragment = (LinksFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        switch (v.getId()) {
            case R.id.drawer_log_in:
                break;
            case R.id.drawer_user_profile:
                break;
            case R.id.drawer_front_page:
                fragment.updateSubreddit(null);
                break;
            case R.id.drawer_r_all:
                fragment.updateSubreddit("all");
                break;
            case R.id.drawer_subreddits:
                break;
            case R.id.drawer_random_subreddit:
                fragment.updateSubreddit("random");
                break;
            case R.id.drawer_navigate_to_subreddit_go:
                String inputSubreddit = mNavToSubredditText.getText().toString();
                if (!inputSubreddit.equals("")) {
                    mNavToSubredditText.setText("");
                    fragment.updateSubreddit(inputSubreddit);
                }
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private void displayFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
