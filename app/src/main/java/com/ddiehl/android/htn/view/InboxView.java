package com.ddiehl.android.htn.view;

import android.support.annotation.NonNull;

public interface InboxView extends LinkView, CommentView, PrivateMessageView {
  void selectTab(@NonNull String show);
}
