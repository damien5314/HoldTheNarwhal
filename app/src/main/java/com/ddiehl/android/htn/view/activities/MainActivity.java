/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view.activities;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
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

import com.ddiehl.android.htn.Analytics;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.io.RedditServiceAuth;
import com.ddiehl.android.htn.presenter.MainPresenter;
import com.ddiehl.android.htn.presenter.MainPresenterImpl;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.dialogs.AnalyticsDialog;
import com.ddiehl.android.htn.view.dialogs.ConfirmSignOutDialog;
import com.ddiehl.android.htn.view.dialogs.NsfwWarningDialog;
import com.ddiehl.android.htn.view.fragments.SettingsFragment;
import com.ddiehl.android.htn.view.fragments.SubredditFragment;
import com.ddiehl.android.htn.view.fragments.UserProfileFragment;
import com.ddiehl.android.htn.view.fragments.WebViewFragment;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.Subreddit;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hugo.weaving.DebugLog;

public class MainActivity extends AppCompatActivity implements MainView,
        NavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = MainActivity.class.getSimpleName();

    public static final int REQUEST_NSFW_WARNING = 0x1;
    private static final String DIALOG_NSFW_WARNING = "dialog_nsfw_warning";
    private static final String DIALOG_CONFIRM_SIGN_OUT = "dialog_confirm_sign_out";
    private static final String DIALOG_ANALYTICS = "dialog_analytics";

    private ProgressDialog mLoadingOverlay;
    private Dialog mSubredditNavigationDialog;

    @Bind(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @Bind(R.id.navigation_view) NavigationView mNavigationView;
    @Bind(R.id.user_account_icon) ImageView mGoldIndicator;
    @Bind(R.id.account_name) TextView mAccountNameView;
    @Bind(R.id.sign_out_button) View mSignOutView;
    @Bind(R.id.navigation_drawer_header_image) ImageView mHeaderImage;

    private Analytics mAnalytics = Analytics.getInstance();
    private MainPresenter mMainPresenter;

    private UserIdentity mCurrentUser;

    @Override @DebugLog
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        
        setMirroredIcons();

        // Listen to events from the navigation drawer
        mNavigationView.setNavigationItemSelectedListener(this);

        // Initialize app toolbar
        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_navigation_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Initialize dependencies
        mMainPresenter = new MainPresenterImpl(this, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMainPresenter.onApplicationStart();
    }

    @Override
    protected void onStop() {
        mMainPresenter.onApplicationStop();
        super.onStop();
    }

    @Override
    public void updateUserIdentity(@Nullable UserIdentity identity) {
        mCurrentUser = identity;
        mAccountNameView.setText(identity == null ?
                getString(R.string.account_name_unauthorized) : identity.getName());
        mSignOutView.setVisibility(identity == null ? View.GONE : View.VISIBLE);
        mGoldIndicator.setVisibility(identity != null && identity.isGold() ? View.VISIBLE : View.GONE);
        updateNavigationItems(identity != null);
    }

    @Override
    public void updateNavigationItems(boolean isLoggedIn) {
        Menu menu = mNavigationView.getMenu();
        menu.findItem(R.id.drawer_log_in).setVisible(!isLoggedIn);
        menu.findItem(R.id.drawer_user_profile).setVisible(isLoggedIn);
        menu.findItem(R.id.drawer_subreddits).setVisible(isLoggedIn);
    }

    @Override
    public void closeNavigationDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.drawer_navigate_to_subreddit:
                showSubredditNavigationDialog();
                mAnalytics.logDrawerNavigateToSubreddit();
                return true;
            case R.id.drawer_log_in:
                showLoginView();
                mAnalytics.logDrawerLogIn();
                return true;
            case R.id.drawer_user_profile:
                String name = mCurrentUser.getName();
                showUserProfile(name);
                mAnalytics.logDrawerUserProfile();
                return true;
            case R.id.drawer_subreddits:
                showUserSubreddits();
                mAnalytics.logDrawerUserSubreddits();
                return true;
            case R.id.drawer_front_page:
                showSubreddit(null);
                mAnalytics.logDrawerFrontPage();
                return true;
            case R.id.drawer_r_all:
                showSubreddit("all");
                mAnalytics.logDrawerAllSubreddits();
                return true;
            case R.id.drawer_random_subreddit:
                showSubreddit("random");
                mAnalytics.logDrawerRandomSubreddit();
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
    public void showUserProfile(@NonNull String username) {
        showUserProfile("summary", username);
    }

    @Override
    public void showUserProfile(@NonNull String show, @NonNull String username) {
        closeNavigationDrawer();
        mMainPresenter.setUsernameContext(username);
        Fragment f = UserProfileFragment.newInstance(show, username);
        showFragment(f);
    }

    @Override
    public void showSubreddit(@Nullable String subreddit) {
        closeNavigationDrawer();
        Fragment f = SubredditFragment.newInstance(subreddit);
        showFragment(f);
    }

    @Override
    public void showWebViewForURL(@NonNull String url) {
        closeNavigationDrawer();
        Fragment f = WebViewFragment.newInstance(url);
        showFragment(f);
    }

    @Override
    public void showUserSubreddits() {
        showToast(R.string.implementation_pending);
    }

    @OnClick(R.id.sign_out_button) @SuppressWarnings("unused")
    void onSignOut() {
        new ConfirmSignOutDialog().show(getFragmentManager(), DIALOG_CONFIRM_SIGN_OUT);
        mAnalytics.logClickedSignOut();
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
    public void showSpinner(@Nullable String message) {
        if (mLoadingOverlay == null) {
            mLoadingOverlay = new ProgressDialog(this, R.style.ProgressDialog);
            mLoadingOverlay.setCancelable(true);
            mLoadingOverlay.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        mLoadingOverlay.setMessage(message);
        mLoadingOverlay.show();
    }

    @Override
    public void showSpinner(@StringRes int resId) {
        showSpinner(getString(resId));
    }

    @Override
    public void dismissSpinner() {
        if (mLoadingOverlay != null && mLoadingOverlay.isShowing()) {
            mLoadingOverlay.dismiss();
        }
    }

    @Override
    public void showToast(@StringRes int resId) {
        showToast(getString(resId));
    }

    @Override
    public void showToast(@NonNull String msg) {
        Snackbar.make(mDrawerLayout, msg, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onSignOutConfirm() {
        mMainPresenter.signOutUser();
    }

    @Override
    public void onSignOutCancel() { /* no-op */ }

    @Override
    public void onSubredditInfoLoaded(@NonNull Subreddit subredditInfo) {
        loadImageIntoDrawerHeader(subredditInfo.getHeaderImageUrl());
    }

    @Override
    public void loadImageIntoDrawerHeader(@Nullable String url) {
        Picasso.with(this)
                .load(url)
                .into(mHeaderImage);
    }

    @Override
    public void showNsfwWarningDialog() {
        DialogFragment dialog = new NsfwWarningDialog();
        dialog.setTargetFragment(getCurrentDisplayedFragment(), REQUEST_NSFW_WARNING);
        dialog.show(getFragmentManager(), DIALOG_NSFW_WARNING);
    }

    public void showSettings() {
        showFragment(new SettingsFragment());
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerVisible(mNavigationView)) {
            mDrawerLayout.closeDrawer(mNavigationView);
            return;
        }

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
                    .setOnClickListener((v) -> {
                        EditText vInput = ButterKnife.findById(mSubredditNavigationDialog,
                                R.id.drawer_navigate_to_subreddit_text);
                        String inputSubreddit = vInput.getText().toString();
                        if (inputSubreddit.equals("")) return;

                        inputSubreddit = inputSubreddit.substring(3);
                        inputSubreddit = inputSubreddit.trim();
                        vInput.setText("");
                        mSubredditNavigationDialog.dismiss();
                        showSubreddit(inputSubreddit);
                    });
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        mSubredditNavigationDialog.show();
    }

    @Override
    public void showSubredditIfEmpty(@Nullable String subreddit) {
        if (getCurrentDisplayedFragment() == null) {
            showSubreddit(subreddit);
        }
    }

    @Override
    public void showAnalyticsRequestDialog() {
        new AnalyticsDialog().show(getFragmentManager(), DIALOG_ANALYTICS);
    }

    @Override
    public void onAnalyticsAccepted() {
        mMainPresenter.onAnalyticsAccepted();
    }

    @Override
    public void onAnalyticsDeclined() {
        mMainPresenter.onAnalyticsDeclined();
    }

    /////////////////
    // Private API //
    /////////////////

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

    private Fragment getCurrentDisplayedFragment() {
        return getFragmentManager().findFragmentById(R.id.fragment_container);
    }

    private void showFragment(@NonNull Fragment f) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction().replace(R.id.fragment_container, f);

        Fragment cf = getCurrentDisplayedFragment();
        if (cf != null) {
            ft.addToBackStack(null);
        }

        ft.commit();
    }
}
