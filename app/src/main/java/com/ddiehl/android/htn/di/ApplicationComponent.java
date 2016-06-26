package com.ddiehl.android.htn.di;

import com.ddiehl.android.htn.analytics.FlurryAnalytics;
import com.ddiehl.android.htn.presenter.BaseListingsPresenter;
import com.ddiehl.android.htn.presenter.MainPresenterImpl;
import com.ddiehl.android.htn.presenter.SettingsPresenterImpl;
import com.ddiehl.android.htn.presenter.UserProfilePresenter;
import com.ddiehl.android.htn.view.activities.BaseActivity;
import com.ddiehl.android.htn.view.activities.PrivateMessageActivity;
import com.ddiehl.android.htn.view.activities.SettingsActivity;
import com.ddiehl.android.htn.view.fragments.BaseListingsFragment;
import com.ddiehl.android.htn.view.fragments.PrivateMessageFragment;
import com.ddiehl.android.htn.view.fragments.SettingsFragment;
import com.ddiehl.android.htn.view.fragments.UserProfileFragment;
import com.ddiehl.android.htn.view.fragments.WebViewFragment;
import com.ddiehl.android.htn.view.viewholders.ListingsCommentViewHolder;
import com.ddiehl.android.htn.view.viewholders.ListingsMessageViewHolder;
import com.ddiehl.android.htn.view.viewholders.ThreadCommentViewHolder;
import com.ddiehl.android.htn.view.viewholders.ThreadStubViewHolder;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
    ApplicationModule.class
})
public interface ApplicationComponent {

  // Managers
  void inject(FlurryAnalytics analytics);

  // Activities
  void inject(BaseActivity activity);
  void inject(PrivateMessageActivity activity);
  void inject(SettingsActivity activity);

  // Fragments
  void inject(BaseListingsFragment fragment);
  void inject(UserProfileFragment fragment);
  void inject(PrivateMessageFragment fragment);
  void inject(SettingsFragment fragment);
  void inject(WebViewFragment fragment);

  // ViewHolders
  void inject(ThreadCommentViewHolder vh);
  void inject(ThreadStubViewHolder vh);
  void inject(ListingsMessageViewHolder vh);
  void inject(ListingsCommentViewHolder vh);

  // Presenters
  void inject(MainPresenterImpl presenter);
  void inject(BaseListingsPresenter presenter);
  void inject(UserProfilePresenter presenter);
  void inject(SettingsPresenterImpl presenter);

}
