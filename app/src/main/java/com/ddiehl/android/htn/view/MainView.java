package com.ddiehl.android.htn.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.ddiehl.android.htn.view.dialogs.AnalyticsDialog;
import com.ddiehl.android.htn.view.dialogs.ConfirmSignOutDialog;
import com.ddiehl.android.htn.view.dialogs.SubredditNavigationDialog;
import com.ddiehl.android.htn.view.fragments.WebViewFragment;
import com.ddiehl.reddit.identity.UserIdentity;

public interface MainView extends AnalyticsDialog.Callbacks, ConfirmSignOutDialog.Callbacks,
    SubredditNavigationDialog.Callbacks, WebViewFragment.Callbacks {
  void updateUserIdentity(@Nullable UserIdentity identity);
  void loadImageIntoDrawerHeader(@Nullable String url);
  void showAnalyticsRequestDialog();
  void showNsfwWarningDialog();
  void showSettings();
  void updateNavigationItems(boolean isLoggedIn);
  void closeNavigationDrawer();
  void showLoginView();
  void showUserProfile(@NonNull String username);
  void showUserProfile(@NonNull String username, @NonNull String show);
  void showUserProfile(@NonNull String username, @NonNull String show, @NonNull String sort);
  void showSubredditNavigationView();
  void showUserSubreddits();
  void showSubreddit(@Nullable String subreddit, @Nullable String sort);
  void showSubredditIfEmpty(@Nullable String subreddit);
  void openURL(@NonNull String url);
  // Methods formerly from BaseView
  void setTitle(@NonNull CharSequence title);
  void showSpinner(@Nullable String msg);
  void showSpinner(@StringRes int resId);
  void dismissSpinner();
  void showToast(@NonNull String msg);
  void showToast(@StringRes int resId);
  void showError(Throwable error, int errorResId);
  void showCommentsForLink(
      @Nullable String subreddit, @Nullable String linkId, @Nullable String commentId);
  void showAboutApp();
}
