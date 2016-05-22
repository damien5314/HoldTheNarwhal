package com.ddiehl.android.htn.presenter;

import android.content.Context;

import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.SettingsManager;
import com.ddiehl.android.htn.utils.AndroidUtils;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.SettingsView;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rxreddit.api.RedditService;
import rxreddit.model.UserIdentity;

public class SettingsPresenterImpl implements SettingsPresenter, IdentityManager.Callbacks {

  @Inject protected Context mApplicationContext;
  @Inject protected RedditService mRedditService;
  @Inject protected IdentityManager mIdentityManager;
  @Inject protected SettingsManager mSettingsManager;
  private MainView mMainView;
  private SettingsView mSettingsView;

  public SettingsPresenterImpl(MainView mainView, SettingsView settingsView) {
    mMainView = mainView;
    mSettingsView = settingsView;
  }

  @Override
  public void onResume() {
    mIdentityManager.registerUserIdentityChangeListener(this);
    if (mRedditService.isUserAuthorized()) {
      refresh(true);
    }
  }

  @Override
  public void onPause() {
    mIdentityManager.unregisterUserIdentityChangeListener(this);
  }

  @Override
  public void onViewDestroyed() { /* no-op */ }

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
      if (AndroidUtils.isConnectedToNetwork(mApplicationContext)) {
        getData();
      } else {
        mMainView.showToast(R.string.error_network_unavailable);
      }
    }
  }

  private void getData() {
    mMainView.showSpinner(null);
    mRedditService.getUserSettings()
        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .doOnTerminate(mMainView::dismissSpinner)
        .doOnNext(mSettingsManager::saveUserSettings)
        .subscribe(settings -> refresh(false),
            e -> mMainView.showError(e, R.string.error_get_user_settings));
  }

  @Override
  public boolean isRefreshable() {
    return mSettingsManager.hasFromRemote();
  }
}
