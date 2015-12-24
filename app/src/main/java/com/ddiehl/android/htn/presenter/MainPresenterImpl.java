package com.ddiehl.android.htn.presenter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.ddiehl.android.dlogger.Logger;
import com.ddiehl.android.htn.AccessTokenManager;
import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.SettingsManager;
import com.ddiehl.android.htn.analytics.Analytics;
import com.ddiehl.android.htn.io.RedditAuthService;
import com.ddiehl.android.htn.io.RedditService;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.reddit.identity.AccessToken;
import com.ddiehl.reddit.identity.UserIdentity;

import java.util.List;

import rx.functions.Action1;

public class MainPresenterImpl implements MainPresenter, IdentityManager.Callbacks {
  private Logger mLogger = HoldTheNarwhal.getLogger();
  private Context mContext = HoldTheNarwhal.getContext();
  private RedditService mRedditService = HoldTheNarwhal.getRedditService();
  private RedditAuthService mRedditAuthService = HoldTheNarwhal.getRedditServiceAuth();
  private AccessTokenManager mAccessTokenManager = HoldTheNarwhal.getAccessTokenManager();
  private IdentityManager mIdentityManager = HoldTheNarwhal.getIdentityManager();
  private SettingsManager mSettingsManager = HoldTheNarwhal.getSettingsManager();
  private Analytics mAnalytics = HoldTheNarwhal.getAnalytics();
  private MainView mMainView;
  private Uri mDeepLink;

  public MainPresenterImpl(MainView view, Uri deepLink) {
    mMainView = view;
    mDeepLink = deepLink;
  }

  @Override
  public void onResume() {
    mIdentityManager.registerUserIdentityChangeListener(this);
    UserIdentity user = getAuthorizedUser();
    mMainView.updateUserIdentity(user);
    mAnalytics.setUserIdentity(user == null ? null : user.getName());

    boolean b = user != null && user.getName() != null;
    mMainView.updateNavigationItems(b);

    if (!showAnalyticsRequestIfNeverShown()) {
      mAnalytics.startSession();
      showFirstView();
    }
  }

  private void showFirstView() {
    if (mDeepLink != null) {
      processDeepLink(mDeepLink);
    } else {
      mMainView.showSubredditIfEmpty(null);
//      mMainView.showCommentsForLink("damien5314apitest", "3xfn0h", null);
    }
  }

  @Override
  public void onPause() {
    mAnalytics.endSession();
    mIdentityManager.unregisterUserIdentityChangeListener(this);
  }

  @Override
  public Action1<UserIdentity> onUserIdentityChanged() {
    return identity -> {
      if (identity == null) {
        mMainView.updateUserIdentity(null);
        mMainView.showToast(R.string.user_signed_out);
      } else {
        // FIXME Ensure we only show this when the user changes
        String name = identity.getName();
        String toast = String.format(
            HoldTheNarwhal.getContext().getString(R.string.welcome_user),
            name);
        mMainView.showToast(toast);
      }
    };
  }

  @Override
  public void onNavigateToSubreddit() {
    mMainView.showSubredditNavigationView();
    mAnalytics.logDrawerNavigateToSubreddit();
  }

  @Override
  public void onLogIn() {
    if (isConnectedToNetwork()) mMainView.showLoginView();
    else mMainView.showToast(R.string.error_no_network);
    mAnalytics.logDrawerLogIn();
  }

  @Override
  public void onShowUserProfile() {
    String name = mIdentityManager.getUserIdentity().getName();
    mMainView.showUserProfile(name);
    mAnalytics.logDrawerUserProfile();
  }

  @Override
  public void onShowSubreddits() {
    mMainView.showUserSubreddits();
    mAnalytics.logDrawerUserSubreddits();
  }

  @Override
  public void onShowFrontPage() {
    mMainView.showSubreddit(null, null);
    mAnalytics.logDrawerFrontPage();
  }

  @Override
  public void onShowAllListings() {
    mMainView.showSubreddit("all", null);
    mAnalytics.logDrawerAllSubreddits();
  }

  @Override
  public void onShowRandomSubreddit() {
    mMainView.showSubreddit("random", null);
    mAnalytics.logDrawerRandomSubreddit();
  }

  private boolean isConnectedToNetwork() {
    ConnectivityManager cm =
        (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo info = cm.getActiveNetworkInfo();
    return info != null && info.isConnectedOrConnecting();
  }

  @Override
  public void signOutUser() {
    mMainView.closeNavigationDrawer();
    mAccessTokenManager.clearSavedUserAccessToken();
    mIdentityManager.clearSavedUserIdentity();
    mAnalytics.logSignOut();
  }

  private boolean showAnalyticsRequestIfNeverShown() {
    if (!mSettingsManager.askedForAnalytics()) {
      mMainView.showAnalyticsRequestDialog();
      return true;
    }
    return false;
  }

  @Override
  public void onAnalyticsAccepted() {
    mSettingsManager.setAskedForAnalytics(true);
    mSettingsManager.setAnalyticsEnabled(true);
    mAnalytics.startSession();
    showFirstView();
  }

  @Override
  public void onAnalyticsDeclined() {
    mSettingsManager.setAskedForAnalytics(true);
    mSettingsManager.setAnalyticsEnabled(false);
    mAnalytics.endSession();
    showFirstView();
  }

  @Override
  public boolean customTabsEnabled() {
    return mSettingsManager.customTabsEnabled();
  }

  @Override
  public void onAuthCodeReceived(String authCode) {
    String grantType = "authorization_code";
    mRedditAuthService.getUserAccessToken(grantType, authCode, RedditAuthService.REDIRECT_URI)
        .doOnNext(mAccessTokenManager.saveUserAccessToken())
        .subscribe(getUserIdentity(), error -> mMainView.showToast(R.string.error_authentication));
  }

  @Override
  public Action1<AccessToken> getUserIdentity() {
    return token -> mRedditService.getUserIdentity()
        .doOnNext(mIdentityManager::saveUserIdentity)
        .subscribe(mMainView::updateUserIdentity,
            e -> mMainView.showError(e, R.string.error_get_user_identity));
  }

  @Override
  public void processDeepLink(@NonNull Uri data) {
    mDeepLink = null;
    mLogger.d("Deep Link: " + data.toString());
    List<String> segments = data.getPathSegments();
    if (segments.size() == 0) {
      // Front page
      mMainView.showSubreddit(null, null);
      return;
    } else if (isSubredditSort(segments.get(0))) {
      // Sorted front page
      mMainView.showSubreddit(null, segments.get(0));
      return;
    } else if (segments.get(0).equals("r")) {
      // Subreddit navigation
      String subreddit = segments.get(1);
      // Check for more metadata
      if (segments.size() > 2) {
        if (segments.get(2).equals("comments")) {
          // Navigating to comment thread
          if (segments.size() > 5) {
            // Link to specific comment
            mMainView.showCommentsForLink(subreddit, segments.get(3), segments.get(5));
            return;
          } else {
            // Link to full thread
            mMainView.showCommentsForLink(subreddit, segments.get(3), null);
            return;
          }
        } else if (isSubredditSort(segments.get(2))) {
          // Subreddit sorted
          mMainView.showSubreddit(subreddit, segments.get(2));
          return;
        }
      } else {
        // Subreddit default sort
        mMainView.showSubreddit(subreddit, null);
        return;
      }
    } else if (segments.get(0).equals("u") || segments.get(0).equals("user")) {
      // User profile navigation
      if (segments.size() > 2) {
        // Profile view specified
        if (segments.size() > 3) {
          // Profile view with sort
          // FIXME This actually should be read from a query string
          mMainView.showUserProfile(segments.get(1), segments.get(2), segments.get(3));
          return;
        } else {
          // Profile view default sort
          mMainView.showUserProfile(segments.get(1), segments.get(2));
          return;
        }
      } else {
        mMainView.showUserProfile(segments.get(1));
        return;
      }
    }
    mLogger.w("Deep link fell through without redirection: " + data.toString());
    mMainView.showSubreddit(null, null); // Show front page
  }

  private boolean isSubredditSort(String s) {
    return s.equals("hot")
        || s.equals("new")
        || s.equals("rising")
        || s.equals("controversial")
        || s.equals("top")
        || s.equals("gilded");
  }

  private UserIdentity getAuthorizedUser() {
    return mIdentityManager.getUserIdentity();
  }
}
