package com.ddiehl.android.htn.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.View;

import rxreddit.model.Link;

public interface LinkView {
  void showLinkContextMenu(ContextMenu menu, View v, Link link);
  void openLinkInWebView(@NonNull Link link);
  void showCommentsForLink(
      @NonNull String subreddit, @NonNull String linkId, @Nullable String commentId);
  void openShareView(@NonNull Link link);
  void openUserProfileView(@NonNull Link link);
  void openLinkInBrowser(@NonNull Link link);
  void openCommentsInBrowser(@NonNull Link link);
}
