package com.ddiehl.android.htn.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.identity.IdentityManager;
import com.ddiehl.android.htn.listings.comments.LinkCommentsActivity;
import com.ddiehl.android.htn.listings.inbox.InboxActivity;
import com.ddiehl.android.htn.listings.inbox.PrivateMessageActivity;
import com.ddiehl.android.htn.listings.profile.UserProfileActivity;
import com.ddiehl.android.htn.listings.subreddit.SubredditActivity;
import com.ddiehl.android.htn.navigation.ConfirmExitDialog;
import com.ddiehl.android.htn.navigation.ConfirmSignOutDialog;
import com.ddiehl.android.htn.navigation.RedditNavigationView;
import com.ddiehl.android.htn.navigation.SubredditNavigationDialog;
import com.ddiehl.android.htn.navigation.WebViewActivity;
import com.ddiehl.android.htn.settings.SettingsActivity;
import com.ddiehl.android.htn.settings.SettingsManager;
import com.ddiehl.android.htn.subscriptions.SubscriptionManagerActivity;
import com.ddiehl.android.htn.utils.AndroidUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rxreddit.android.SignInActivity;
import rxreddit.api.RedditService;
import rxreddit.model.PrivateMessage;
import rxreddit.model.UserAccessToken;
import rxreddit.model.UserIdentity;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public abstract class BaseActivity extends AppCompatActivity implements
        RedditNavigationView,
        IdentityManager.Callbacks,
        NavigationView.OnNavigationItemSelectedListener,
        ConfirmExitDialog.Callbacks,
        ConfirmSignOutDialog.Callbacks,
        SubredditNavigationDialog.Callbacks {

    public static final int REQUEST_SIGN_IN = 2;

    private static final String EXTRA_CUSTOM_TABS_SESSION =
            "android.support.customtabs.extra.SESSION";
    private static final String EXTRA_CUSTOM_TABS_TOOLBAR_COLOR =
            "android.support.customtabs.extra.TOOLBAR_COLOR";
    private static final String EXTRA_AUTHENTICATION_STATE_CHANGE =
            "com.ddiehl.android.htn.EXTRA_AUTHENTICATION_STATE_CHANGE";

    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.navigation_view) NavigationView mNavigationView;
    @BindView(R.id.tab_layout) TabLayout mTabLayout;
    /* @BindView(R.id.user_account_icon) */ ImageView mGoldIndicator;
    /* @BindView(R.id.account_name) */ TextView mAccountNameView;
    /* @BindView(R.id.sign_out_button) */ View mSignOutView;
    /* @BindView(R.id.navigation_drawer_header_image) */ ImageView mHeaderImage;

    @Inject protected RedditService mRedditService;
    @Inject protected IdentityManager mIdentityManager;
    @Inject protected SettingsManager mSettingsManager;
    @Inject protected Gson mGson;

    ActionBarDrawerToggle mDrawerToggle;

    protected abstract boolean hasNavigationDrawer();

    @Override
    protected void attachBaseContext(Context newBase) {
        ContextWrapper wrapper = CalligraphyContextWrapper.wrap(newBase);
        super.attachBaseContext(wrapper);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HoldTheNarwhal.getApplicationComponent().inject(this);
        ButterKnife.bind(this);

        // Initialize toolbar
        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (hasNavigationDrawer()) {
                mDrawerToggle = new ActionBarDrawerToggle(
                        this,                   /* host Activity */
                        mDrawerLayout,          /* DrawerLayout object */
                        toolbar,                /* nav drawer icon to replace 'Up' caret */
                        R.string.drawer_open,   /* "open drawer" description */
                        R.string.drawer_close   /* "close drawer" description */
                );

                // Set the drawer toggle as the DrawerListener
                mDrawerLayout.addDrawerListener(mDrawerToggle);

                Drawable homeIndicator = AndroidUtils.getTintedDrawable(
                        this, R.drawable.ic_menu_black_24dp, R.color.icons
                );
                actionBar.setHomeAsUpIndicator(homeIndicator);
            } else {
                actionBar.setHomeAsUpIndicator(null);
            }
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Initialize navigation view
        initNavigationView();

        /**
         * FIXME
         * This is a hack because sometimes the user gets stuck in a state where
         * they don't have a valid access token, but still have their user identity.
         */
        if (!mRedditService.isUserAuthorized()) {
            mIdentityManager.clearSavedUserIdentity();
        }

        // Check to see if the activity was restarted due to authentication change
        checkAndShowAuthenticationStateChange();

        // Custom font
        CalligraphyConfig.initDefault(
                new CalligraphyConfig.Builder()
                        .setDefaultFontPath(getFont())
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

        showTabs(false);
    }

    private String getFont() {
        return mSettingsManager.getFont();
    }

    private void checkAndShowAuthenticationStateChange() {
        if (getIntent().getExtras() == null ||
                !getIntent().getExtras().containsKey(EXTRA_AUTHENTICATION_STATE_CHANGE)) return;

        boolean isAuthenticated = getIntent().getBooleanExtra(EXTRA_AUTHENTICATION_STATE_CHANGE, false);

        if (!isAuthenticated) {
            showToast(getString(R.string.user_signed_out));
        } else {
            String name = mIdentityManager.getUserIdentity().getName();
            String formatter = getString(R.string.welcome_user);
            String toast = String.format(formatter, name);
            showToast(toast);
        }
    }

    // Workaround for bug in support lib 23.1.0 - 23.2.0
    // https://code.google.com/p/android/issues/detail?id=190226
    private void initNavigationView() {
        mNavigationView = ButterKnife.findById(this, R.id.navigation_view);
        View header = mNavigationView.inflateHeaderView(R.layout.navigation_drawer_header);
        mGoldIndicator = (ImageView) header.findViewById(R.id.user_account_icon);
        mAccountNameView = (TextView) header.findViewById(R.id.account_name);
        mSignOutView = header.findViewById(R.id.sign_out_button);
        mHeaderImage = (ImageView) header.findViewById(R.id.navigation_drawer_header_image);

        mSignOutView.setOnClickListener(view -> onSignOut());
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mIdentityManager.registerUserIdentityChangeListener(this);
        UserIdentity user = mIdentityManager.getUserIdentity();
        updateUserIdentity(user);

        boolean isLoggedIn = user != null && user.getName() != null;
        updateNavigationItems(isLoggedIn);
    }

    @Override
    protected void onPause() {
        mIdentityManager.unregisterUserIdentityChangeListener(this);
        super.onPause();
    }

    @Override
    public Action1<UserIdentity> onUserIdentityChanged() {
        return identity -> {
            // Restart activity
            Intent newIntent = (Intent) getIntent().clone();
            boolean isAuthenticated = identity != null;

            // Clear the task stack if we're no longer authenticated
            // This prevents the user from going back to content they aren't allowed to see
            newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

            // Add extra to indicate that this activity was restarted due to a change in authentication state
            newIntent.putExtra(EXTRA_AUTHENTICATION_STATE_CHANGE, isAuthenticated);

            startActivity(newIntent);
        };
    }

    protected void updateUserIdentity(@Nullable UserIdentity identity) {
        mAccountNameView.setText(identity == null ?
                getString(R.string.account_name_unauthorized) : identity.getName());
        mSignOutView.setVisibility(identity == null ? GONE : VISIBLE);
        mGoldIndicator.setVisibility(identity != null && identity.isGold() ? VISIBLE : GONE);
        updateNavigationItems(identity != null);
    }

    protected void showTabs(boolean show) {
        mTabLayout.setVisibility(show ? VISIBLE : GONE);
    }

    protected void updateNavigationItems(boolean isLoggedIn) {
        Menu menu = mNavigationView.getMenu();
        menu.findItem(R.id.drawer_log_in).setVisible(!isLoggedIn);
        menu.findItem(R.id.drawer_inbox).setVisible(isLoggedIn);
        menu.findItem(R.id.drawer_user_profile).setVisible(isLoggedIn);
        menu.findItem(R.id.drawer_subreddits).setVisible(isLoggedIn);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        closeNavigationDrawer();
        switch (menuItem.getItemId()) {
            case R.id.drawer_navigate_to_subreddit:
                onNavigateToSubreddit();
                return true;
            case R.id.drawer_log_in:
                onLogIn();
                return true;
            case R.id.drawer_inbox:
                onShowInbox();
                return true;
            case R.id.drawer_user_profile:
                onShowUserProfile();
                return true;
            case R.id.drawer_subreddits:
                onShowSubreddits();
                return true;
            case R.id.drawer_front_page:
                onShowFrontPage();
                return true;
            case R.id.drawer_r_all:
                onShowAllListings();
                return true;
            case R.id.drawer_random_subreddit:
                onShowRandomSubreddit();
                return true;
        }
        return false;
    }

    private void closeNavigationDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    protected void onNavigateToSubreddit() {
        showSubredditNavigationView();
    }

    protected void onLogIn() {
        if (AndroidUtils.isConnectedToNetwork(this)) {
            showLoginView();
        } else {
            showToast(getString(R.string.error_network_unavailable));
        }
    }

    protected void onShowInbox() {
        showInbox();
    }

    protected void onShowUserProfile() {
        String name = mIdentityManager.getUserIdentity().getName();
        showUserProfile(name, "summary", "new");
    }

    protected void onShowSubreddits() {
        showUserSubreddits();
    }

    protected void onShowFrontPage() {
        showSubreddit(null, null, null);
    }

    protected void onShowAllListings() {
        showSubreddit("all", null, null);
    }

    protected void onShowRandomSubreddit() {
        showSubreddit("random", null, null);
    }

    public void showLoginView() {
        Intent intent = new Intent(this, SignInActivity.class);
        intent.putExtra(SignInActivity.EXTRA_AUTH_URL, mRedditService.getAuthorizationUrl());
        startActivityForResult(intent, REQUEST_SIGN_IN);
    }

    public void showInbox() {
        Intent intent = InboxActivity.getIntent(this, null);
        startActivity(intent);
    }

    public void showUserProfile(
            @NonNull String username, @Nullable String show, @Nullable String sort) {
        Intent intent = UserProfileActivity.getIntent(this, username, show, sort);
        startActivity(intent);
    }

    public void showSubreddit(@Nullable String subreddit, @Nullable String sort, String timespan) {
        Intent intent = SubredditActivity.getIntent(this, subreddit, sort, timespan);
        startActivity(intent);
    }

    @SuppressLint("NewApi")
    public void openURL(@NonNull String url) {
        if (canUseCustomTabs()) {
            // If so, present URL in custom tabs instead of WebView
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            Bundle extras = new Bundle();
            // Pass IBinder instead of null for a custom tabs session
            extras.putBinder(EXTRA_CUSTOM_TABS_SESSION, null);
            extras.putInt(EXTRA_CUSTOM_TABS_TOOLBAR_COLOR, ContextCompat.getColor(this, R.color.primary));
            intent.putExtras(extras);

            // Check if Activity exists to handle the Intent
            // Should resolve https://fabric.io/projects11111111111476634619/android/apps/com.ddiehl.android.htn/issues/583bb8cd0aeb16625b5bed8c
            if (intent.resolveActivity(getPackageManager()) != null) {
                Timber.e("No Activity found that can handle custom tabs Intent");
                startActivity(intent);
                return;
            }
        }

        // Show web view for URL if we didn't show it in custom tabs
        showWebViewForURL(url);
    }

    private void showWebViewForURL(@NonNull String url) {
        Intent intent = WebViewActivity.getIntent(this, url);
        startActivity(intent);
    }

    private boolean canUseCustomTabs() {
        return Build.VERSION.SDK_INT >= 18 && customTabsEnabled();
    }

    private boolean customTabsEnabled() {
        return mSettingsManager.customTabsEnabled();
    }

    public void showUserSubreddits() {
        Intent intent = SubscriptionManagerActivity.getIntent(this);
        startActivity(intent);
    }

    //  @OnClick(R.id.sign_out_button)
    void onSignOut() {
        new ConfirmSignOutDialog().show(getSupportFragmentManager(), ConfirmSignOutDialog.TAG);
    }

    public void signOutUser() {
        closeNavigationDrawer();
        mRedditService.revokeAuthentication();
        mIdentityManager.clearSavedUserIdentity();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle other option items
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onSignOutConfirm() {
        signOutUser();
    }

    @Override
    public void onSignOutCancel() {
        // no-op
    }

    @Override
    public void showSubredditImage(String url) {
        Glide.with(this)
                .load(url)
                .into(mHeaderImage);
    }

    @Override
    public void showSettings() {
        Intent intent = SettingsActivity.getIntent(this);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerVisible(mNavigationView)) {
            closeNavigationDrawer();
            return;
        }
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            if (isTaskRoot()) showExitConfirmation();
            else super.onBackPressed();
        }
    }

    private void showExitConfirmation() {
        new ConfirmExitDialog().show(getSupportFragmentManager(), ConfirmExitDialog.TAG);
    }

    @Override
    public void onConfirmExit() {
        super.onBackPressed();
    }

    @Override
    public void onCancelExit() {
    }

    @Override
    public void onSubredditNavigationConfirmed(String subreddit) {
        showSubreddit(subreddit, null, null);
    }

    @Override
    public void onSubredditNavigationCancelled() {
    }

    @Override
    public void showSubredditNavigationView() {
        new SubredditNavigationDialog().show(getSupportFragmentManager(), SubredditNavigationDialog.TAG);
    }

    @Override
    public void showCommentsForLink(
            @Nullable String subreddit, @Nullable String linkId, @Nullable String commentId) {
        Intent intent = LinkCommentsActivity.getIntent(this, subreddit, linkId, commentId);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_SIGN_IN:
                if (resultCode == Activity.RESULT_OK) {
                    String url = data.getStringExtra(SignInActivity.EXTRA_CALLBACK_URL);
                    onSignInCallback(url);
                }
                break;
        }
    }

    private void onSignInCallback(String url) {
        mRedditService.processAuthenticationCallback(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(getUserIdentity())
                .subscribe(
                        result -> { },
                        error -> {
                            if (error instanceof IOException) {
                                String message = getString(R.string.error_network_unavailable);
                                showError(message);
                            } else {
                                Timber.w(error, "Error during sign in");
                                showError(getString(R.string.error_get_user_identity));
                            }
                        }
                );
    }

    private void showError(String message) {
        Snackbar.make(mDrawerLayout, message, Snackbar.LENGTH_LONG).show();
    }

    private Func1<UserAccessToken, Observable<UserIdentity>> getUserIdentity() {
        return token -> mRedditService.getUserIdentity()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(mIdentityManager::saveUserIdentity);
    }

    @Override
    public void showInboxMessages(@NonNull List<PrivateMessage> messages) {
        Intent intent = PrivateMessageActivity.getIntent(this, mGson, messages);
        startActivity(intent);
    }

    @Override
    public boolean onKeyUp(int keycode, KeyEvent e) {
        switch (keycode) {
            case KeyEvent.KEYCODE_MENU:
                if (getSupportActionBar() != null) {
                    getSupportActionBar().openOptionsMenu();
                    return true;
                }
        }
        return super.onKeyUp(keycode, e);
    }

    protected void showToast(@NonNull String message) {
        Snackbar.make(mDrawerLayout, message, Snackbar.LENGTH_SHORT).show();
    }
}
