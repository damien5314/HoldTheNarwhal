package com.ddiehl.android.htn.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.ddiehl.android.htn.view.dialogs.AnalyticsDialog;
import com.ddiehl.android.htn.view.dialogs.ConfirmSignOutDialog;
import com.ddiehl.android.htn.view.dialogs.SubredditNavigationDialog;

import java.util.List;

import rxreddit.model.PrivateMessage;
import rxreddit.model.UserIdentity;

public interface MainView extends AnalyticsDialog.Callbacks, ConfirmSignOutDialog.Callbacks,
    SubredditNavigationDialog.Callbacks {

  void updateUserIdentity(@Nullable UserIdentity identity);
  void loadImageIntoDrawerHeader(@Nullable String url);
  void showAnalyticsRequestDialog();
  void showNsfwWarningDialog();
  void showSettings();
  void updateNavigationItems(boolean isLoggedIn);
  void closeNavigationDrawer();
  void showLoginView();
  void showInbox();
  void showInboxMessages(@NonNull List<PrivateMessage> messages);
  void showUserProfile(@NonNull String username, @Nullable String show, @Nullable String sort);
  void showSubredditNavigationView();
  void showUserSubreddits();
  void showSubreddit(@Nullable String subreddit, @Nullable String sort, String timespan);
  void showSubredditIfEmpty(@Nullable String subreddit);
  void showCommentsForLink(
      @NonNull String subreddit, @NonNull String linkId, @Nullable String commentId);
  void openURL(@NonNull String url);
  // Methods formerly from BaseView
  void setTitle(@NonNull CharSequence title);
  void setTitle(@StringRes int id);
  void showSpinner(@Nullable String msg);
  void showSpinner(@StringRes int resId);
  void dismissSpinner();
  void showToast(@NonNull String msg);
  void showToast(@StringRes int resId);
  void showError(Throwable error, int errorResId);
  void showAboutApp();
  void goBack();
  void resetBackNavigation();

}
