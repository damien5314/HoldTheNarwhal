package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;

import com.ddiehl.android.htn.AccessTokenManager;
import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.SettingsManager;
import com.ddiehl.android.htn.io.RedditService;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.fragments.SettingsView;
import com.ddiehl.reddit.identity.UserIdentity;

import rx.functions.Action1;

public class SettingsPresenterImpl implements SettingsPresenter, IdentityManager.Callbacks {
  private RedditService mRedditService = HoldTheNarwhal.getRedditService();
  private AccessTokenManager mAccessTokenManager = HoldTheNarwhal.getAccessTokenManager();
  private IdentityManager mIdentityManager = HoldTheNarwhal.getIdentityManager();
  private SettingsManager mSettingsManager = HoldTheNarwhal.getSettingsManager();
  private MainView mMainView;
  private SettingsView mSettingsView;

  public SettingsPresenterImpl(@NonNull MainView mainView, @NonNull SettingsView settingsView) {
    mMainView = mainView;
    mSettingsView = settingsView;
  }

  @Override
  public void onResume() {
    mIdentityManager.registerUserIdentityChangeListener(this);
    if (mAccessTokenManager.isUserAuthorized()) {
      refresh(true);
    }
  }

  @Override
  public void onPause() {
    mIdentityManager.unregisterUserIdentityChangeListener(this);
  }

  @Override
  public Action1<UserIdentity> onUserIdentityChanged() {
    return identity -> {
      boolean shouldRefresh = identity != null;
      refresh(shouldRefresh);
    };
  }

  @Override
  public void refresh(boolean pullFromServer) {
    boolean showUser = mSettingsManager.hasFromRemote();
    mSettingsView.showPreferences(showUser);
    if (pullFromServer) {
      getData();
    }
  }

  private void getData() {
    mMainView.showSpinner(null);
    mRedditService.getUserSettings()
        .doOnTerminate(mMainView::dismissSpinner)
        .doOnError(mMainView::showError)
        .doOnNext(mSettingsManager::saveUserSettings)
        .subscribe(settings -> refresh(false));
  }

  @Override
  public boolean isRefreshable() {
    return mSettingsManager.hasFromRemote();
  }
}
