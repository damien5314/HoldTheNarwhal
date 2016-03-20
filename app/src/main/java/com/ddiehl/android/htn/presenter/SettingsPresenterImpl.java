package com.ddiehl.android.htn.presenter;

import android.content.Context;
import android.support.annotation.NonNull;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.SettingsManager;
import com.ddiehl.android.htn.utils.AndroidUtils;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.SettingsView;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rxreddit.api.RedditService;
import rxreddit.model.UserIdentity;

public class SettingsPresenterImpl implements SettingsPresenter, IdentityManager.Callbacks {
  private RedditService mRedditService = HoldTheNarwhal.getRedditService();
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
      Context context = HoldTheNarwhal.getContext();
      if (AndroidUtils.isConnectedToNetwork(context)) {
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
