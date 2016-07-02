package com.ddiehl.android.htn.view.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
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
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.analytics.Analytics;
import com.ddiehl.android.htn.presenter.MainPresenter;
import com.ddiehl.android.htn.presenter.MainPresenterImpl;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.dialogs.AnalyticsDialog;
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
import rxreddit.android.SignInActivity;
import rxreddit.model.PrivateMessage;
import rxreddit.model.UserIdentity;
import timber.log.Timber;

public abstract class BaseActivity extends AppCompatActivity
    implements MainView, NavigationView.OnNavigationItemSelectedListener, ConfirmExitDialog.Callbacks {

  public static final int REQUEST_SIGN_IN = 2;

  private static final String DIALOG_CONFIRM_SIGN_OUT = "dialog_confirm_sign_out";
  private static final String DIALOG_ANALYTICS = "dialog_analytics";
  private static final String DIALOG_SUBREDDIT_NAVIGATION = "dialog_subreddit_navigation";
  private static final String EXTRA_CUSTOM_TABS_SESSION =
      "android.support.customtabs.extra.SESSION";
  private static final String EXTRA_CUSTOM_TABS_TOOLBAR_COLOR =
      "android.support.customtabs.extra.TOOLBAR_COLOR";

  @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
  @BindView(R.id.navigation_view) NavigationView mNavigationView;
//  @BindView(R.id.user_account_icon)
  ImageView mGoldIndicator;
//  @BindView(R.id.account_name)
  TextView mAccountNameView;
//  @BindView(R.id.sign_out_button)
  View mSignOutView;
//  @BindView(R.id.navigation_drawer_header_image)
  ImageView mHeaderImage;
  private ProgressDialog mLoadingOverlay;

  @Inject protected Analytics mAnalytics;
  @Inject protected Gson mGson;
  private MainPresenter mMainPresenter;

  public abstract void showFragment();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    HoldTheNarwhal.getApplicationComponent().inject(this);
    setContentView(R.layout.activity_main);
    initNavigationView();
    ButterKnife.bind(this);

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

    setTitle(null);

    // Initialize dependencies
    Uri data = getIntent().getData();
    mMainPresenter = new MainPresenterImpl(this, data);
  }

  // Workaround for bug in support lib 23.1.0 - 23.2.0
  // https://code.google.com/p/android/issues/detail?id=190226
  private void initNavigationView() {
    mNavigationView = ButterKnife.findById(this, R.id.navigation_view);
    View header = mNavigationView.inflateHeaderView(R.layout.navigation_drawer_header);
    mGoldIndicator = (ImageView) header.findViewById(R.id.user_account_icon);
    mAccountNameView = (TextView) header.findViewById(R.id.account_name);
    mSignOutView = header.findViewById(R.id.sign_out_button);
    mSignOutView.setOnClickListener(view -> onSignOut());
    mHeaderImage = (ImageView) header.findViewById(R.id.navigation_drawer_header_image);
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMainPresenter.onResume();
  }

  @Override
  protected void onPause() {
    mMainPresenter.onPause();
    super.onPause();
  }

  @Override
  public void updateUserIdentity(@Nullable UserIdentity identity) {
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
    menu.findItem(R.id.drawer_inbox).setVisible(isLoggedIn);
    menu.findItem(R.id.drawer_user_profile).setVisible(isLoggedIn);
    menu.findItem(R.id.drawer_subreddits).setVisible(isLoggedIn);
  }

  @Override
  public void closeNavigationDrawer() {
    mDrawerLayout.closeDrawer(GravityCompat.START);
  }

  @Override
  public boolean onNavigationItemSelected(MenuItem menuItem) {
    closeNavigationDrawer();
    switch (menuItem.getItemId()) {
      case R.id.drawer_navigate_to_subreddit:
        mMainPresenter.onNavigateToSubreddit();
        return true;
      case R.id.drawer_log_in:
        mMainPresenter.onLogIn();
        return true;
      case R.id.drawer_inbox:
        mMainPresenter.onShowInbox();
        return true;
      case R.id.drawer_user_profile:
        mMainPresenter.onShowUserProfile();
        return true;
      case R.id.drawer_subreddits:
        mMainPresenter.onShowSubreddits();
        return true;
      case R.id.drawer_front_page:
        mMainPresenter.onShowFrontPage();
        return true;
      case R.id.drawer_r_all:
        mMainPresenter.onShowAllListings();
        return true;
      case R.id.drawer_random_subreddit:
        mMainPresenter.onShowRandomSubreddit();
        return true;
    }
    return false;
  }

  @Override
  public void showLoginView() {
    Intent data = new Intent(this, SignInActivity.class);
    data.putExtra(SignInActivity.EXTRA_AUTH_URL, mMainPresenter.getAuthorizationUrl());
    startActivityForResult(data, REQUEST_SIGN_IN);
  }

  @Override
  public void showInbox() {
    Intent intent = InboxActivity.getIntent(this, null);
    startActivity(intent);
  }

  @Override
  public void showUserProfile(
      @NonNull String username, @Nullable String show, @Nullable String sort) {
    Intent intent = UserProfileActivity.getIntent(this, username, show, sort);
    startActivity(intent);
  }

  @Override
  public void showSubreddit(@Nullable String subreddit, @Nullable String sort, String timespan) {
    Intent intent = SubredditActivity.getIntent(this, subreddit, sort, timespan);
    startActivity(intent);
  }

  @Override @SuppressLint("NewApi")
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
    return Build.VERSION.SDK_INT >= 18 && mMainPresenter.customTabsEnabled();
  }

  @Override
  public void showUserSubreddits() {
    showToast(R.string.implementation_pending);
  }

//  @OnClick(R.id.sign_out_button)
  void onSignOut() {
    new ConfirmSignOutDialog().show(getSupportFragmentManager(), DIALOG_CONFIRM_SIGN_OUT);
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

  private int mDialogCount = 0;

  @Override
  public void showSpinner(@Nullable String message) {
    mDialogCount++;
    if (mLoadingOverlay == null) {
      mLoadingOverlay = new ProgressDialog(this, R.style.ProgressDialog);
      mLoadingOverlay.setCancelable(false);
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
    mDialogCount--;
    if (mDialogCount < 0) mDialogCount = 0;
    if (mLoadingOverlay != null && mLoadingOverlay.isShowing() && mDialogCount == 0) {
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
  public void loadImageIntoDrawerHeader(@Nullable String url) {
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
  public void goBack() {
    onBackPressed();
  }

  @Override
  public void showSubredditIfEmpty(@Nullable String subreddit) {
    if (getCurrentDisplayedFragment() == null) {
      showSubreddit(subreddit, null, null);
    }
  }

  @Override
  public void showAnalyticsRequestDialog() {
    new AnalyticsDialog().show(getSupportFragmentManager(), DIALOG_ANALYTICS);
  }

  @Override
  public void onAnalyticsAccepted() {
    mMainPresenter.onAnalyticsAccepted();
  }

  @Override
  public void onAnalyticsDeclined() {
    mMainPresenter.onAnalyticsDeclined();
  }

  @Override
  public void onSubredditNavigationConfirmed(String subreddit) {
    showSubreddit(subreddit, null, null);
  }

  @Override
  public void onSubredditNavigationCancelled() { /* no-op */ }

  @Override
  public void showError(Throwable error, int errorResId) {
    String message = getString(errorResId);
    Timber.e(error, message);
    if (error instanceof UnknownHostException) {
      message = getString(R.string.error_network_unavailable);
    }
    Snackbar.make(mDrawerLayout, message, Snackbar.LENGTH_LONG).show();
  }

  @Override
  public void showSubredditNavigationView() {
    new SubredditNavigationDialog().show(getSupportFragmentManager(), DIALOG_SUBREDDIT_NAVIGATION);
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
          String callbackUrl = data.getStringExtra(SignInActivity.EXTRA_CALLBACK_URL);
          mMainPresenter.onSignIn(callbackUrl);
        }
        break;
    }
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
}
