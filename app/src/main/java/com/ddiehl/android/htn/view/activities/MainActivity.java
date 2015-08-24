/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ddiehl.android.htn.AccessTokenManager;
import com.ddiehl.android.htn.BusProvider;
import com.ddiehl.android.htn.HTNAnalytics;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.RedditPrefs;
import com.ddiehl.android.htn.SettingsManager;
import com.ddiehl.android.htn.events.requests.UserSignOutEvent;
import com.ddiehl.android.htn.events.responses.UserAuthCodeReceivedEvent;
import com.ddiehl.android.htn.io.RedditService;
import com.ddiehl.android.htn.io.RedditServiceAuth;
import com.ddiehl.android.htn.presenter.MainPresenter;
import com.ddiehl.android.htn.presenter.MainPresenterImpl;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.dialogs.ConfirmSignOutDialog;
import com.ddiehl.android.htn.view.fragments.SettingsFragment;
import com.ddiehl.android.htn.view.fragments.SubredditFragment;
import com.ddiehl.android.htn.view.fragments.UserProfileFragment;
import com.ddiehl.android.htn.view.fragments.WebViewFragment;
import com.ddiehl.reddit.identity.UserIdentity;
import com.squareup.otto.Bus;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hugo.weaving.DebugLog;

public class MainActivity extends AppCompatActivity
        implements MainView, ConfirmSignOutDialog.Callbacks, NavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = MainActivity.class.getSimpleName();

    private static final String DIALOG_CONFIRM_SIGN_OUT = "dialog_confirm_sign_out";

    private Bus mBus = BusProvider.getInstance();
    private MainPresenter mMainPresenter;

    private ProgressDialog mProgressBar;
    private Dialog mSubredditNavigationDialog;
    private Dialog mAnalyticsRequestDialog;

    @Bind(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @Bind(R.id.navigation_view) NavigationView mNavigationView;
    @Bind(R.id.user_account_icon) ImageView mGoldIndicator;
    @Bind(R.id.account_name) TextView mAccountNameView;
    @Bind(R.id.sign_out_button) View mSignOutView;

    private AccessTokenManager mAccessTokenManager;
    private IdentityManager mIdentityManager;
    private SettingsManager mSettingsManager;
    private RedditService mAuthProxy;
    private HTNAnalytics mAnalytics;

    @Override @DebugLog
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configure Flurry
        HTNAnalytics.initializeFlurry(this);

        ButterKnife.bind(MainActivity.this);
        mNavigationView.setNavigationItemSelectedListener(MainActivity.this);

        // Initialize app toolbar
        Toolbar toolbar = ButterKnife.findById(MainActivity.this, R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_navigation_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mMainPresenter = new MainPresenterImpl(MainActivity.this, MainActivity.this);
        mAccessTokenManager = AccessTokenManager.getInstance(MainActivity.this);
        mIdentityManager = IdentityManager.getInstance(MainActivity.this);
        mSettingsManager = SettingsManager.getInstance(MainActivity.this);
        mAuthProxy = RedditServiceAuth.getInstance(MainActivity.this);
        mAnalytics = HTNAnalytics.getInstance();

        // Configure MoPub
//        new MoPubConversionTracker().reportAppOpen(MainActivity.this);
//        MoPub.setLocationAwareness(MoPub.LocationAwareness.DISABLED);
//        InMobi.initialize(this, "7a754516768e4a0e9c3af91f1fc9ebea");

        setMirroredIcons();
    }

    private void setMirroredIcons() {
        if (Build.VERSION.SDK_INT >= 19) {
            int[] ids = new int[] {
                    R.drawable.ic_action_refresh,
                    R.drawable.ic_sign_out,
                    R.drawable.ic_action_reply,
                    R.drawable.ic_action_save,
                    R.drawable.ic_action_share,
                    R.drawable.ic_action_show_comments,
                    R.drawable.ic_change_sort,
                    R.drawable.ic_change_timespan,
                    R.drawable.ic_navigation_go,
                    R.drawable.ic_saved,
                    R.drawable.ic_saved_dark
            };

            for (int id : ids) {
                Drawable res = ContextCompat.getDrawable(this, id);
                if (res != null) {
                    res.setAutoMirrored(true);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mBus.register(mMainPresenter);
        mBus.register(mAccessTokenManager);
        mBus.register(mIdentityManager);
        mBus.register(mSettingsManager);
        mBus.register(mAuthProxy);
        mBus.register(mAnalytics);
        updateUserIdentity();
        if (!showAnalyticsRequestIfNeverShown()) {
            HTNAnalytics.startSession(this);
            showSubredditIfEmpty(null);
        }
    }

    @Override
    protected void onStop() {
        HTNAnalytics.endSession(this);
        mBus.unregister(mMainPresenter);
        mBus.unregister(mAccessTokenManager);
        mBus.unregister(mIdentityManager);
        mBus.unregister(mSettingsManager);
        mBus.unregister(mAuthProxy);
        mBus.unregister(mAnalytics);

        super.onStop();
    }

    @Override
    public void updateUserIdentity() {
        UserIdentity identity = mMainPresenter.getAuthorizedUser();
        mAccountNameView.setText(identity == null ?
                getString(R.string.account_name_unauthorized) : identity.getName());
        mSignOutView.setVisibility(identity == null ? View.GONE : View.VISIBLE);
        mGoldIndicator.setVisibility(identity != null && identity.isGold() ? View.VISIBLE : View.GONE);
        updateNavigationItems();
        HTNAnalytics.setUserIdentity(identity == null ? null : identity.getName());
    }

    @Override
    public void closeNavigationDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private void updateNavigationItems() {
        Menu menu = mNavigationView.getMenu();
        UserIdentity user = mMainPresenter.getAuthorizedUser();
        boolean b = user != null && user.getName() != null;
        menu.findItem(R.id.drawer_log_in).setVisible(!b);
        menu.findItem(R.id.drawer_user_profile).setVisible(b);
        menu.findItem(R.id.drawer_subreddits).setVisible(b);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.drawer_navigate_to_subreddit:
                showSubredditNavigationDialog();
                HTNAnalytics.logDrawerNavigateToSubreddit();
                return true;
            case R.id.drawer_log_in:
                showLoginView();
                HTNAnalytics.logDrawerLogIn();
                return true;
            case R.id.drawer_user_profile:
                showUserProfile();
                HTNAnalytics.logDrawerUserProfile();
                return true;
            case R.id.drawer_subreddits:
                showUserSubreddits();
                HTNAnalytics.logDrawerUserSubreddits();
                return true;
            case R.id.drawer_front_page:
                showSubreddit(null);
                HTNAnalytics.logDrawerFrontPage();
                return true;
            case R.id.drawer_r_all:
                showSubreddit("all");
                HTNAnalytics.logDrawerAllSubreddits();
                return true;
            case R.id.drawer_random_subreddit:
                showSubreddit("random");
                HTNAnalytics.logDrawerRandomSubreddit();
                return true;
        }
        return false;
    }

    @Override
    public void showLoginView() {
        closeNavigationDrawer();
        showWebViewForURL(RedditServiceAuth.AUTHORIZATION_URL);
    }

    @Override
    public void showUserProfile() {
        UserIdentity user = mMainPresenter.getAuthorizedUser();
        if (user != null) {
            String username = user.getName();
            showUserProfile("summary", username);
        } else {
            mBus.post(new UserSignOutEvent());
        }
    }

    @Override
    public void showUserProfile(String show, String username) {
        closeNavigationDrawer();
        mMainPresenter.setUsernameContext(username);
        Fragment f = UserProfileFragment.newInstance(show, username);
        showFragment(f);
    }

    @Override
    public void showSubreddit(String subreddit) {
        closeNavigationDrawer();
        Fragment f = SubredditFragment.newInstance(subreddit);
        showFragment(f);
    }

    @Override
    public void showWebViewForURL(String url) {
        closeNavigationDrawer();
        Fragment f = WebViewFragment.newInstance(url);
        showFragment(f);
    }

    @Override
    public void showUserSubreddits() {
        showToast(R.string.implementation_pending);
    }

    @OnClick(R.id.sign_out_button)
    void onSignOut() {
        ConfirmSignOutDialog dialog = ConfirmSignOutDialog.newInstance();
        dialog.show(getFragmentManager(), DIALOG_CONFIRM_SIGN_OUT);
        HTNAnalytics.logClickedSignOut();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return false;
    }

    @Override
    public void showSpinner(String message) {
        if (mProgressBar == null) {
            mProgressBar = new ProgressDialog(this, R.style.ProgressDialog);
            mProgressBar.setCancelable(true);
            mProgressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        mProgressBar.setMessage(message);
        mProgressBar.show();
    }

    @Override
    public void showSpinner(int resId) {
        showSpinner(getString(resId));
    }

    @Override
    public void dismissSpinner() {
        if (mProgressBar != null && mProgressBar.isShowing()) {
            mProgressBar.dismiss();
        }
    }

    @Override
    public void showToast(int resId) {
        showToast(getString(resId));
    }

    @Override
    public void showToast(String s) {
        Snackbar.make(mDrawerLayout, s, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onUserAuthCodeReceived(String authCode) {
        getFragmentManager().popBackStack();

        // Notify auth API about the auth code retrieval
        mBus.post(new UserAuthCodeReceivedEvent(authCode));
    }

    @Override
    public void onSignOutConfirm() {
        mMainPresenter.signOutUser();
    }

    @Override
    public void onSignOutCancel() {
        // Do nothing
    }

    public void showSettings() {
        showFragment(new SettingsFragment());
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private void showSubredditNavigationDialog() {
        if (mSubredditNavigationDialog == null) {
            mSubredditNavigationDialog = new Dialog(this);
            mSubredditNavigationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mSubredditNavigationDialog.setContentView(R.layout.navigate_to_subreddit_edit_text);
            ButterKnife.findById(mSubredditNavigationDialog, R.id.drawer_navigate_to_subreddit_go)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditText vInput = ButterKnife.findById(mSubredditNavigationDialog,
                                    R.id.drawer_navigate_to_subreddit_text);
                            String inputSubreddit = vInput.getText().toString();
                            if (inputSubreddit.equals("")) return;

                            inputSubreddit = inputSubreddit.substring(3);
                            inputSubreddit = inputSubreddit.trim();
                            vInput.setText("");
                            mSubredditNavigationDialog.dismiss();
                            showSubreddit(inputSubreddit);
                        }
                    });
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        mSubredditNavigationDialog.show();
    }

    private void showSubredditIfEmpty(String subreddit) {
        FragmentManager fm = getFragmentManager();
        Fragment f = fm.findFragmentById(R.id.fragment_container);
        if (f == null) {
            showSubreddit(subreddit);
        }
    }

    private void showFragment(Fragment f) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction().replace(R.id.fragment_container, f);

        Fragment cf = fm.findFragmentById(R.id.fragment_container);
        if (cf != null) {
            ft.addToBackStack(null);
        }

        ft.commit();
    }

    private boolean showAnalyticsRequestIfNeverShown() {
        if (!RedditPrefs.askedForAnalytics(this)) {
            showAnalyticsRequestDialog();
            return true;
        }
        return false;
    }

    private void showAnalyticsRequestDialog() {
        if (mAnalyticsRequestDialog == null) {
            mAnalyticsRequestDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_analytics_title)
                    .setMessage(R.string.dialog_analytics_message)
                    .setNeutralButton(R.string.dialog_analytics_accept, (dialog, which) -> {
                        RedditPrefs.setAnalyticsEnabled(this, true);
                        RedditPrefs.setAskedForAnalytics(this, true);
                        HTNAnalytics.startSession(this);
                        showSubredditIfEmpty(null);
                    })
                    .setNegativeButton(R.string.dialog_analytics_decline, (dialog, which) -> {
                        RedditPrefs.setAnalyticsEnabled(this, false);
                        RedditPrefs.setAskedForAnalytics(this, true);
                        HTNAnalytics.endSession(this);
                        showSubredditIfEmpty(null);
                    })
                    .create();
        }
        mAnalyticsRequestDialog.show();
    }

    private void dismissAnalyticsRequestDialog() {
        if (mAnalyticsRequestDialog != null && mAnalyticsRequestDialog.isShowing()) {
            mAnalyticsRequestDialog.dismiss();
        }
    }

}
