package com.ddiehl.android.htn.view.activities;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ddiehl.android.dlogger.Logger;
import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.analytics.Analytics;
import com.ddiehl.android.htn.io.RedditAuthService;
import com.ddiehl.android.htn.presenter.MainPresenter;
import com.ddiehl.android.htn.presenter.MainPresenterImpl;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.dialogs.AnalyticsDialog;
import com.ddiehl.android.htn.view.dialogs.ConfirmSignOutDialog;
import com.ddiehl.android.htn.view.dialogs.NsfwWarningDialog;
import com.ddiehl.android.htn.view.dialogs.SubredditNavigationDialog;
import com.ddiehl.android.htn.view.fragments.AboutAppFragment;
import com.ddiehl.android.htn.view.fragments.InboxFragment;
import com.ddiehl.android.htn.view.fragments.LinkCommentsFragment;
import com.ddiehl.android.htn.view.fragments.PrivateMessageFragment;
import com.ddiehl.android.htn.view.fragments.SettingsFragment;
import com.ddiehl.android.htn.view.fragments.SubredditFragment;
import com.ddiehl.android.htn.view.fragments.UserProfileFragment;
import com.ddiehl.android.htn.view.fragments.WebViewFragment;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.PrivateMessage;
import com.squareup.picasso.Picasso;

import java.net.UnknownHostException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MainView,
    NavigationView.OnNavigationItemSelectedListener {
  public static final int REQUEST_NSFW_WARNING = 0x1;
  private static final String DIALOG_NSFW_WARNING = "dialog_nsfw_warning";
  private static final String DIALOG_CONFIRM_SIGN_OUT = "dialog_confirm_sign_out";
  private static final String DIALOG_ANALYTICS = "dialog_analytics";
  private static final String DIALOG_SUBREDDIT_NAVIGATION = "dialog_subreddit_navigation";
  private static final String EXTRA_CUSTOM_TABS_SESSION =
      "android.support.customtabs.extra.SESSION";
  private static final String EXTRA_CUSTOM_TABS_TOOLBAR_COLOR =
      "android.support.customtabs.extra.TOOLBAR_COLOR";

  @Bind(R.id.drawer_layout) DrawerLayout mDrawerLayout;
  @Bind(R.id.navigation_view) NavigationView mNavigationView;
//  @Bind(R.id.user_account_icon)
  ImageView mGoldIndicator;
//  @Bind(R.id.account_name)
  TextView mAccountNameView;
//  @Bind(R.id.sign_out_button)
  View mSignOutView;
//  @Bind(R.id.navigation_drawer_header_image)
  ImageView mHeaderImage;
  private ProgressDialog mLoadingOverlay;

  private Logger mLogger = HoldTheNarwhal.getLogger();
  private Analytics mAnalytics = HoldTheNarwhal.getAnalytics();
  private MainPresenter mMainPresenter;
  private boolean mBackStackReset = true;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
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
    showWebViewForURL(RedditAuthService.AUTHORIZATION_URL);
  }

  @Override
  public void showInbox() {
    InboxFragment fragment = InboxFragment.newInstance(null);
    showFragment(fragment);
  }

  @Override
  public void showUserProfile(
      @NonNull String username, @Nullable String show, @Nullable String sort) {
    Fragment f = UserProfileFragment.newInstance(username, show, sort);
    showFragment(f);
  }

  @Override
  public void showSubreddit(@Nullable String subreddit, @Nullable String sort) {
    Fragment f = SubredditFragment.newInstance(subreddit, sort);
    showFragment(f);
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
    Fragment f = WebViewFragment.newInstance(url);
    showFragment(f);
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
  public void showNsfwWarningDialog() {
    DialogFragment dialog = new NsfwWarningDialog();
    dialog.setTargetFragment(getCurrentDisplayedFragment(), REQUEST_NSFW_WARNING);
    dialog.show(getFragmentManager(), DIALOG_NSFW_WARNING);
  }

  @Override
  public void showSettings() {
    showFragment(new SettingsFragment());
  }

  @Override
  public void onBackPressed() {
    if (mDrawerLayout.isDrawerVisible(mNavigationView)) {
      closeNavigationDrawer();
      return;
    }
    FragmentManager fm = getFragmentManager();
    if (fm.getBackStackEntryCount() > 0) {
      fm.popBackStack();
    } else {
      // TODO Exit confirmation dialog
      super.onBackPressed();
    }
  }

  @Override
  public void goBack() {
    onBackPressed();
  }

  @Override
  public void showSubredditIfEmpty(@Nullable String subreddit) {
    if (getCurrentDisplayedFragment() == null) {
      showSubreddit(subreddit, null);
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

  @Override
  public void onSubredditNavigationConfirmed(String subreddit) {
    resetBackNavigation();
    showSubreddit(subreddit, null);
  }

  @Override
  public void onSubredditNavigationCancelled() { /* no-op */ }

  @Override
  public void onAuthCodeReceived(String authCode) {
    mMainPresenter.onAuthCodeReceived(authCode);
  }

  @Override
  public void showError(Throwable error, int errorResId) {
    String message = getString(errorResId);
    mLogger.e(error, message);
    if (error instanceof UnknownHostException) {
      message = getString(R.string.error_network_unavailable);
    }
    Snackbar.make(mDrawerLayout, message, Snackbar.LENGTH_LONG).show();
  }

  @Override
  public void showSubredditNavigationView() {
    new SubredditNavigationDialog().show(getFragmentManager(), DIALOG_SUBREDDIT_NAVIGATION);
  }

  @Override
  public void showCommentsForLink(
      @Nullable String subreddit, @Nullable String linkId, @Nullable String commentId) {
    Fragment fragment = LinkCommentsFragment.newInstance(subreddit, linkId, commentId);
    showFragment(fragment);
  }

  @Override
  public void showAboutApp() {
    Fragment fragment = AboutAppFragment.newInstance();
    showFragment(fragment);
  }

  private Fragment getCurrentDisplayedFragment() {
    return getFragmentManager().findFragmentById(R.id.fragment_container);
  }

  private void showFragment(@NonNull Fragment f) {
    @SuppressLint("CommitTransaction")
    FragmentTransaction ft = getFragmentManager().beginTransaction()
        .replace(R.id.fragment_container, f);
    if (getCurrentDisplayedFragment() != null && !mBackStackReset) ft.addToBackStack(null);
    ft.commit();
    mBackStackReset = false;
  }

  @Override
  public void resetBackNavigation() {
    mBackStackReset = true;
    getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
  }

  @Override
  public void showInboxMessages(@NonNull List<PrivateMessage> messages) {
    PrivateMessageFragment fragment = PrivateMessageFragment.newInstance(messages);
    showFragment(fragment);
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
