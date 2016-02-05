package com.ddiehl.android.htn.view;

import android.support.annotation.NonNull;

import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.Listing;

import java.util.List;

public interface UserProfileView extends LinkView, CommentView {
  void showUserInfo(@NonNull UserIdentity user);
  void showFriendNote(@NonNull String note);
  void hideFriendNote();
  void showTrophies(List<Listing> trophies);
  void setFriendButtonState(boolean isFriend);
  void selectTab(String show);
  void refreshTabs(boolean showAuthenticatedTabs);
}
