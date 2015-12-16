package com.ddiehl.android.htn.presenter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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
  private String mUsernameContext;

  public MainPresenterImpl(MainView view) {
    mMainView = view;
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
      mMainView.showSubredditIfEmpty(null);
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
  public void onLogIn() {
    if (isConnectedToNetwork()) mMainView.showLoginView();
    else mMainView.showToast(R.string.error_no_network);
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

  @Override
  public void setUsernameContext(String username) {
    mUsernameContext = username;
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
    mMainView.showSubredditIfEmpty(null);
  }

  @Override
  public void onAnalyticsDeclined() {
    mSettingsManager.setAskedForAnalytics(true);
    mSettingsManager.setAnalyticsEnabled(false);
    mAnalytics.endSession();
    mMainView.showSubredditIfEmpty(null);
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

  private UserIdentity getAuthorizedUser() {
    return mIdentityManager.getUserIdentity();
  }
}
