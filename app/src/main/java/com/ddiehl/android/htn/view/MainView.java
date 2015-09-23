package com.ddiehl.android.htn.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.ddiehl.android.htn.view.dialogs.AnalyticsDialog;
import com.ddiehl.android.htn.view.dialogs.ConfirmSignOutDialog;
import com.ddiehl.android.htn.view.dialogs.SubredditNavigationDialog;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.Subreddit;

public interface MainView extends AnalyticsDialog.Callbacks, ConfirmSignOutDialog.Callbacks,
        SubredditNavigationDialog.Callbacks {

    void updateUserIdentity(@Nullable UserIdentity identity);
    void loadImageIntoDrawerHeader(@Nullable String url);
    void showAnalyticsRequestDialog();
    void showNsfwWarningDialog();

    void updateNavigationItems(boolean isLoggedIn);

    void closeNavigationDrawer();
    void showLoginView();
    void showUserProfile(@NonNull String username);
    void showUserProfile(@NonNull String show, @NonNull String username);
    void showUserSubreddits();
    void showSubreddit(@Nullable String subreddit);
    void showSubredditIfEmpty(@Nullable String subreddit);
    void showWebViewForURL(@NonNull String url);

    // Methods formerly from BaseView
    void setTitle(@NonNull CharSequence title);
    void showSpinner(@Nullable String msg);
    void showSpinner(@StringRes int resId);
    void dismissSpinner();
    void showToast(@NonNull String msg);
    void showToast(@StringRes int resId);
    void onSubredditInfoLoaded(@NonNull Subreddit subredditInfo);

}
