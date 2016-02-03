package com.ddiehl.android.htn.view;

import android.support.annotation.NonNull;
import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.reddit.listings.PrivateMessage;

public interface PrivateMessageView {
  void showPrivateMessageContextMenu(ContextMenu menu, View v, PrivateMessage privateMessage);
  void showSubject(@NonNull String subject);
}