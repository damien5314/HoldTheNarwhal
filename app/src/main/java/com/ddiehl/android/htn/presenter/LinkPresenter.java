package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;
import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.android.htn.ThumbnailMode;

import rxreddit.model.Link;

public interface LinkPresenter extends BasePresenter {
  void showLinkContextMenu(ContextMenu menu, View view, Link link);
  void openLink(@NonNull Link link);
  void showCommentsForLink(@NonNull Link link);
  void showCommentsForLink();
  void replyToLink();
  void upvoteLink();
  void downvoteLink();
  void saveLink();
  void unsaveLink();
  void shareLink();
  void openLinkSubreddit();
  void openLinkUserProfile();
  void openLinkUserProfile(@NonNull Link link);
  void openLinkInBrowser();
  void openCommentsInBrowser();
  void hideLink();
  void unhideLink();
  void reportLink();
  boolean shouldShowNsfwTag();
  ThumbnailMode getThumbnailMode();
}
