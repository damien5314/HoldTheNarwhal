package com.ddiehl.android.htn.presenter;

import android.content.Context;
import android.support.annotation.NonNull;

import com.ddiehl.android.htn.AccessTokenManager;
import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.SettingsManager;
import com.ddiehl.android.htn.SettingsManagerImpl;
import com.ddiehl.android.htn.io.RedditService;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.fragments.SettingsFragment;
import com.ddiehl.reddit.identity.UserIdentity;

import rx.functions.Action1;

public class SettingsPresenter implements BasePresenter, IdentityManager.Callbacks {
  private RedditService mRedditService = HoldTheNarwhal.getRedditService();
  private AccessTokenManager mAccessTokenManager = HoldTheNarwhal.getAccessTokenManager();
  private IdentityManager mIdentityManager = HoldTheNarwhal.getIdentityManager();
  private SettingsManager mSettingsManager = HoldTheNarwhal.getSettingsManager();
  private Context mContext = HoldTheNarwhal.getContext();
  private MainView mMainView;
  private SettingsFragment mSettingsView;

  public SettingsPresenter(@NonNull MainView mainView, @NonNull SettingsFragment settingsView) {
    mMainView = mainView;
    mSettingsView = settingsView;
  }

  @Override
  public void onResume() {
    mIdentityManager.registerUserIdentityChangeListener(this);
    mContext.getSharedPreferences(SettingsManagerImpl.PREFS_USER, Context.MODE_PRIVATE)
        .registerOnSharedPreferenceChangeListener(this);
    if (mAccessTokenManager.isUserAuthorized()) {
      refresh(true);
    }
  }

  @Override
  public void onPause() {
    mIdentityManager.unregisterUserIdentityChangeListener(this);
    mContext.getSharedPreferences(SettingsManagerImpl.PREFS_USER, Context.MODE_PRIVATE)
        .unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public Action1<UserIdentity> onUserIdentityChanged() {
    return identity -> {
      boolean shouldRefresh = identity != null;
      refresh(shouldRefresh);
    };
  }

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

  public boolean isRefreshable() {
    return mSettingsManager.hasFromRemote();
  }
}
