package com.ddiehl.android.htn.view;

import android.support.annotation.NonNull;

public interface InboxView extends ListingsView, LinkView, CommentView, PrivateMessageView {

  void selectTab(@NonNull String show);

  String getShow();

  // Call this when the user no longer has permission to view inbox
  void finish();
}
