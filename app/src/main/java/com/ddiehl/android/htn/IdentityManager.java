package com.ddiehl.android.htn;

import com.ddiehl.reddit.identity.UserIdentity;

import rx.functions.Action1;

public interface IdentityManager {
  UserIdentity getUserIdentity();
  void saveUserIdentity(UserIdentity identity);
  void clearSavedUserIdentity();

  void registerUserIdentityChangeListener(Callbacks listener);
  void unregisterUserIdentityChangeListener(Callbacks listener);

  interface Callbacks {
    Action1<UserIdentity> onUserIdentityChanged();
  }
}
