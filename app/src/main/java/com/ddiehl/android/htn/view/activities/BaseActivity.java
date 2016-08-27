package com.ddiehl.android.htn.view.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.SettingsManager;
import com.ddiehl.android.htn.analytics.Analytics;
import com.ddiehl.android.htn.subscriptions.SubscriptionManagerActivity;
import com.ddiehl.android.htn.utils.AndroidUtils;
import com.ddiehl.android.htn.view.RedditNavigationView;
import com.ddiehl.android.htn.view.dialogs.ConfirmExitDialog;
import com.ddiehl.android.htn.view.dialogs.ConfirmSignOutDialog;
import com.ddiehl.android.htn.view.dialogs.SubredditNavigationDialog;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.net.UnknownHostException;
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

  @BindView(R.id.drawer_layout)                         protected DrawerLayout mDrawerLayout;
  @BindView(R.id.navigation_view)                       protected NavigationView mNavigationView;
  /* @BindView(R.id.user_account_icon) */               protected ImageView mGoldIndicator;
  /* @BindView(R.id.account_name) */                    protected TextView mAccountNameView;
  /* @BindView(R.id.sign_out_button) */                 protected View mSignOutView;
  /* @BindView(R.id.navigation_drawer_header_image) */  protected ImageView mHeaderImage;

  @Inject protected RedditService mRedditService;
  @Inject protected IdentityManager mIdentityManager;
  @Inject protected SettingsManager mSettingsManager;
  @Inject protected Analytics mAnalytics;
  @Inject protected Gson mGson;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    HoldTheNarwhal.getApplicationComponent().inject(this);
    ButterKnife.bind(this);

    initNavigationView();

    // Listen to events from the navigation drawer
    mNavigationView.setNavigationItemSelectedListener(this);

    // Initialize app toolbar
    Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
    setSupportActionBar(toolbar);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      Drawable homeIndicator = HoldTheNarwhal.getTintedDrawable(
          this, R.drawable.ic_menu_black_24dp, R.color.icons);
      actionBar.setHomeAsUpIndicator(homeIndicator);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    setTitle(null);

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
  }

  @Override
  protected void onResume() {
    super.onResume();
    
    mIdentityManager.registerUserIdentityChangeListener(this);
    UserIdentity user = mIdentityManager.getUserIdentity();
    updateUserIdentity(user);
    
    boolean isLoggedIn = user != null && user.getName() != null;
    updateNavigationItems(isLoggedIn);

    mAnalytics.setUserIdentity(user == null ? null : user.getName());
    mAnalytics.startSession();
  }

  @Override
  protected void onPause() {
    mIdentityManager.unregisterUserIdentityChangeListener(this);
    mAnalytics.endSession();
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

  protected void updateNavigationItems(boolean isLoggedIn) {
    Menu menu = mNavigationView.getMenu();
    menu.findItem(R.id.drawer_log_in).setVisible(!isLoggedIn);
    menu.findItem(R.id.drawer_inbox).setVisible(isLoggedIn);
    menu.findItem(R.id.drawer_user_profile).setVisible(isLoggedIn);
    menu.findItem(R.id.drawer_subreddits).setVisible(isLoggedIn);
  }

  @Override
  public boolean onNavigationItemSelected(MenuItem menuItem) {
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
    mAnalytics.logDrawerNavigateToSubreddit();
  }

  protected void onLogIn() {
    if (AndroidUtils.isConnectedToNetwork(this)) {
      showLoginView();
    } else showToast(getString(R.string.error_network_unavailable));

    mAnalytics.logDrawerLogIn();
  }

  protected void onShowInbox() {
    showInbox();
    mAnalytics.logDrawerShowInbox();
  }

  protected void onShowUserProfile() {
    String name = mIdentityManager.getUserIdentity().getName();
    showUserProfile(name, "summary", "new");
    mAnalytics.logDrawerUserProfile();
  }

  protected void onShowSubreddits() {
    showUserSubreddits();
    mAnalytics.logDrawerUserSubreddits();
  }

  protected void onShowFrontPage() {
    showSubreddit(null, null, null);
    mAnalytics.logDrawerFrontPage();
  }

  protected void onShowAllListings() {
    showSubreddit("all", null, null);
    mAnalytics.logDrawerAllSubreddits();
  }

  protected void onShowRandomSubreddit() {
    showSubreddit("random", null, null);
    mAnalytics.logDrawerRandomSubreddit();
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
      startActivity(intent);
    } else showWebViewForURL(url);
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
    mAnalytics.logClickedSignOut();
  }

  public void signOutUser() {
    closeNavigationDrawer();
    mRedditService.revokeAuthentication();
    mIdentityManager.clearSavedUserIdentity();
    mAnalytics.logSignOut();
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
  public void onSignOutConfirm() {
    signOutUser();
  }

  @Override
  public void onSignOutCancel() { }

  @Override
  public void showSubredditImage(String url) {
    Picasso.with(this)
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
  public void onCancelExit() { }

  @Override
  public void onSubredditNavigationConfirmed(String subreddit) {
    showSubreddit(subreddit, null, null);
  }

  @Override
  public void onSubredditNavigationCancelled() { }

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
              Timber.e(error, "Error during sign in");
              showError(error, getString(R.string.error_get_user_identity));
            }
        );
  }

  private void showError(Throwable error, String message) {
    if (error instanceof UnknownHostException) {
      message = getString(R.string.error_network_unavailable);
    }
    Snackbar.make(mDrawerLayout, message, Snackbar.LENGTH_LONG).show();
  }

  private Func1<UserAccessToken, Observable<UserIdentity>> getUserIdentity() {
    return token -> mRedditService.getUserIdentity()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(mIdentityManager::saveUserIdentity);
  }

  private Fragment getCurrentDisplayedFragment() {
    return getSupportFragmentManager().findFragmentById(R.id.fragment_container);
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
        if ( getSupportActionBar() != null ) {
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
