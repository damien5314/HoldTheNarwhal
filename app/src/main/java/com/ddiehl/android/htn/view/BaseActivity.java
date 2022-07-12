package com.ddiehl.android.htn.view;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.gallery.MediaGalleryFragment;
import com.ddiehl.android.htn.identity.IdentityManager;
import com.ddiehl.android.htn.listings.inbox.InboxActivity;
import com.ddiehl.android.htn.listings.inbox.PrivateMessageActivity;
import com.ddiehl.android.htn.listings.profile.UserProfileActivity;
import com.ddiehl.android.htn.listings.subreddit.SubredditActivity;
import com.ddiehl.android.htn.managers.NetworkConnectivityManager;
import com.ddiehl.android.htn.navigation.ConfirmExitDialog;
import com.ddiehl.android.htn.navigation.ConfirmSignOutDialog;
import com.ddiehl.android.htn.navigation.RedditNavigationView;
import com.ddiehl.android.htn.navigation.SubredditNavigationDialog;
import com.ddiehl.android.htn.navigation.WebViewActivity;
import com.ddiehl.android.htn.routing.AuthRouter;
import com.ddiehl.android.htn.settings.SettingsActivity;
import com.ddiehl.android.htn.settings.SettingsManager;
import com.ddiehl.android.htn.subscriptions.SubscriptionManagerActivity;
import com.ddiehl.android.htn.utils.AndroidUtils;
import com.ddiehl.android.htn.utils.ThemeUtilsKt;
import com.ddiehl.android.htn.view.glide.GlideApp;
import com.ddiehl.android.htn.view.theme.ColorScheme;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import rxreddit.android.SignInActivity;
import rxreddit.api.RedditService;
import rxreddit.model.GalleryItem;
import rxreddit.model.PrivateMessage;
import rxreddit.model.UserAccessToken;
import rxreddit.model.UserIdentity;
import timber.log.Timber;

public abstract class BaseActivity extends BaseDaggerActivity implements
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

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TabLayout tabLayout;
    private ImageView goldIndicator;
    private TextView accountNameView;
    private View signOutView;
    private ImageView headerImage;

    @Inject
    RedditService redditService;
    @Inject
    IdentityManager identityManager;
    @Inject
    SettingsManager settingsManager;
    @Inject
    Gson gson;
    @Inject
    NetworkConnectivityManager networkConnectivityManager;
    @Inject
    AuthRouter authRouter;

    ActionBarDrawerToggle drawerToggle;
    private ColorScheme currentColorScheme;

    protected abstract boolean hasNavigationDrawer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        // Color scheme needs to be applied before the call to super.onCreate, otherwise we can get in
        // weird states where the background is in default theme but the foreground is in another
        applyColorScheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        tabLayout = findViewById(R.id.tab_layout);

        // Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (hasNavigationDrawer()) {
                drawerToggle = new ActionBarDrawerToggle(
                        this,                   /* host Activity */
                        drawerLayout,          /* DrawerLayout object */
                        toolbar,                /* nav drawer icon to replace 'Up' caret */
                        R.string.drawer_open,   /* "open drawer" description */
                        R.string.drawer_close   /* "close drawer" description */
                );

                // Set the drawer toggle as the DrawerListener
                drawerLayout.addDrawerListener(drawerToggle);

                Drawable homeIndicator = AndroidUtils.getTintedDrawable(
                        this, R.drawable.ic_menu_black_24dp, R.attr.iconColor
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
        if (!redditService.isUserAuthorized()) {
            identityManager.clearSavedUserIdentity();
        }

        // Check to see if the activity was restarted due to authentication change
        checkAndShowAuthenticationStateChange();

        showTabs(false);
    }

    private void applyColorScheme() {
        final ColorScheme colorScheme = settingsManager.getColorScheme();
        setTheme(colorScheme.getStyleRes());
        currentColorScheme = colorScheme;
    }

    private void checkAndShowAuthenticationStateChange() {
        if (getIntent().getExtras() == null ||
                !getIntent().getExtras().containsKey(EXTRA_AUTHENTICATION_STATE_CHANGE)) return;

        boolean isAuthenticated = getIntent().getBooleanExtra(EXTRA_AUTHENTICATION_STATE_CHANGE, false);

        if (!isAuthenticated) {
            showToast(getString(R.string.user_signed_out));
        } else {
            String name = identityManager.getUserIdentity().getName();
            String formatter = getString(R.string.welcome_user);
            String toast = String.format(formatter, name);
            showToast(toast);
        }
    }

    // Workaround for bug in support lib 23.1.0 - 23.2.0
    // https://code.google.com/p/android/issues/detail?id=190226
    private void initNavigationView() {
        navigationView = findViewById(R.id.navigation_view);
        View header = navigationView.inflateHeaderView(R.layout.navigation_drawer_header);
        goldIndicator = header.findViewById(R.id.user_account_icon);
        accountNameView = header.findViewById(R.id.account_name);
        signOutView = header.findViewById(R.id.sign_out_button);
        headerImage = header.findViewById(R.id.navigation_drawer_header_image);

        signOutView.setOnClickListener(view -> onSignOut());
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (drawerToggle != null) {
            drawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (drawerToggle != null) {
            drawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        restartIfThemeChanged();
    }

    private void restartIfThemeChanged() {
        final ColorScheme colorScheme = settingsManager.getColorScheme();
        if (currentColorScheme != colorScheme) {
            currentColorScheme = colorScheme;
            recreate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        identityManager.registerUserIdentityChangeListener(this);
        UserIdentity user = identityManager.getUserIdentity();
        updateUserIdentity(user);

        boolean isLoggedIn = user != null && user.getName() != null;
        updateNavigationItems(isLoggedIn);
    }

    @Override
    protected void onPause() {
        identityManager.unregisterUserIdentityChangeListener(this);
        super.onPause();
    }

    @Override
    public void onUserIdentityChanged(UserIdentity identity) {
        // Restart activity
        Intent newIntent = (Intent) getIntent().clone();
        boolean isAuthenticated = identity != null;

        // Clear the task stack if we're no longer authenticated
        // This prevents the user from going back to content they aren't allowed to see
        newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        // Add extra to indicate that this activity was restarted due to a change in authentication state
        newIntent.putExtra(EXTRA_AUTHENTICATION_STATE_CHANGE, isAuthenticated);

        startActivity(newIntent);
    }

    protected void updateUserIdentity(@Nullable UserIdentity identity) {
        accountNameView.setText(identity == null ?
                getString(R.string.account_name_unauthorized) : identity.getName());
        signOutView.setVisibility(identity == null ? GONE : VISIBLE);
        goldIndicator.setVisibility(identity != null && identity.isGold() ? VISIBLE : GONE);
        updateNavigationItems(identity != null);
    }

    protected void showTabs(boolean show) {
        tabLayout.setVisibility(show ? VISIBLE : GONE);
    }

    protected void updateNavigationItems(boolean isLoggedIn) {
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.drawer_log_in).setVisible(!isLoggedIn);
        menu.findItem(R.id.drawer_inbox).setVisible(isLoggedIn);
        menu.findItem(R.id.drawer_user_profile).setVisible(isLoggedIn);
        menu.findItem(R.id.drawer_subreddits).setVisible(isLoggedIn);
    }

    @Override
    public boolean onNavigationItemSelected(@NotNull MenuItem menuItem) {
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
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    protected void onNavigateToSubreddit() {
        showSubredditNavigationView();
    }

    protected void onLogIn() {
        if (networkConnectivityManager.isConnectedToNetwork()) {
            authRouter.showLoginView();
        } else {
            showToast(getString(R.string.error_network_unavailable));
        }
    }

    protected void onShowInbox() {
        showInbox();
    }

    protected void onShowUserProfile() {
        String name = identityManager.getUserIdentity().getName();
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

    public void showInbox() {
        Intent intent = InboxActivity.getIntent(this, null);
        startActivity(intent);
    }

    public void showUserProfile(
            @NotNull String username, @Nullable String show, @Nullable String sort) {
        Intent intent = UserProfileActivity.getIntent(this, username, show, sort);
        startActivity(intent);
    }

    public void showSubreddit(@Nullable String subreddit, @Nullable String sort, String timespan) {
        Intent intent = SubredditActivity.getIntent(this, subreddit, sort, timespan);
        startActivity(intent);
    }

    public void openURL(@NotNull String url) {
        // If so, present URL in custom tabs instead of WebView
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        Bundle extras = new Bundle();
        // Pass IBinder instead of null for a custom tabs session
        extras.putBinder(EXTRA_CUSTOM_TABS_SESSION, null);
        final int toolbarColor = ThemeUtilsKt.getColorFromAttr(this, R.attr.colorPrimary);
        extras.putInt(EXTRA_CUSTOM_TABS_TOOLBAR_COLOR, toolbarColor);
        intent.putExtras(extras);

        // Check if Activity exists to handle the Intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            Timber.e("No Activity found that can handle custom tabs Intent");
            startActivity(intent);
            return;
        }

        // Show web view for URL if we didn't show it in custom tabs
        showWebViewForURL(url);
    }

    private void showWebViewForURL(@NotNull String url) {
        Intent intent = WebViewActivity.getIntent(this, url);
        startActivity(intent);
    }

    @Override
    public void openLinkGallery(@NotNull List<GalleryItem> galleryItems) {
        Timber.d("Opening gallery with item count: %s", galleryItems.size());
        MediaGalleryFragment.create(galleryItems)
                .show(getSupportFragmentManager(), MediaGalleryFragment.TAG);
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
        redditService.revokeAuthentication();
        identityManager.clearSavedUserIdentity();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drawerToggle != null && drawerToggle.onOptionsItemSelected(item)) {
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
        GlideApp.with(this)
                .load(url)
                .into(headerImage);
    }

    @Override
    public void showSettings() {
        Intent intent = SettingsActivity.getIntent(this);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerVisible(navigationView)) {
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
        redditService.processAuthenticationCallback(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(getUserIdentity())
                .subscribe(
                        result -> {
                        },
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
        Snackbar.make(drawerLayout, message, Snackbar.LENGTH_LONG).show();
    }

    private Function<UserAccessToken, Observable<UserIdentity>> getUserIdentity() {
        return token -> redditService.getUserIdentity()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(identityManager::saveUserIdentity);
    }

    @Override
    public void showInboxMessages(@NotNull List<PrivateMessage> messages) {
        Intent intent = PrivateMessageActivity.getIntent(this, gson, messages);
        startActivity(intent);
    }

    protected void showToast(@NotNull String message) {
        Snackbar.make(drawerLayout, message, Snackbar.LENGTH_SHORT).show();
    }
}
