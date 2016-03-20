package com.ddiehl.android.htn.presenter;

import android.view.ContextMenu;
import android.view.View;

import rxreddit.model.PrivateMessage;
import rxreddit.model.UserIdentity;

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
