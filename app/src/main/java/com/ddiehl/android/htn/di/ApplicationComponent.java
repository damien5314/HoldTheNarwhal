package com.ddiehl.android.htn.di;

import com.ddiehl.android.htn.analytics.FlurryAnalytics;
import com.ddiehl.android.htn.presenter.BaseListingsPresenter;
import com.ddiehl.android.htn.presenter.MainPresenterImpl;
import com.ddiehl.android.htn.presenter.UserProfilePresenter;
import com.ddiehl.android.htn.view.activities.BaseActivity;
import com.ddiehl.android.htn.view.activities.PrivateMessageActivity;
import com.ddiehl.android.htn.view.fragments.BaseListingsFragment;
import com.ddiehl.android.htn.view.fragments.PrivateMessageFragment;
import com.ddiehl.android.htn.view.fragments.UserProfileFragment;
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

  // Fragments
  void inject(BaseListingsFragment fragment);
  void inject(UserProfileFragment fragment);
  void inject(PrivateMessageFragment fragment);

  // ViewHolders
  void inject(ThreadCommentViewHolder vh);
  void inject(ThreadStubViewHolder vh);
  void inject(ListingsMessageViewHolder vh);
  void inject(ListingsCommentViewHolder vh);

  // Presenters
  void inject(MainPresenterImpl mainPresenter);
  void inject(BaseListingsPresenter presenter);
  void inject(UserProfilePresenter presenter);

}
