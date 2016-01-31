package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;
import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.PrivateMessage;

public interface MessagePresenter {
  void setSelectedListing(@NonNull Listing listing);
  // FIXME Change this to onItemSelected or something
  void showMessageContextMenu(
      ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, PrivateMessage message);
  UserIdentity getUserIdentity();
  void replyToMessage();
  void markMessageUnread();
  void showMessagePermalink();
  void reportMessage();
  void blockUser();
}
