package com.ddiehl.android.htn.presenter;

import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.PrivateMessage;

public interface MessagePresenter {
  // FIXME Change this to onItemSelected or something
  void showMessageContextMenu(
      ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, PrivateMessage message);
  UserIdentity getUserIdentity();
  void replyToMessage();
  void markMessageRead();
  void markMessageUnread();
  void showMessagePermalink();
  void showMessagePermalink(PrivateMessage message);
  void reportMessage();
  void blockUser();
}
