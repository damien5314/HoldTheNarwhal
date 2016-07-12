package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;

import com.ddiehl.android.htn.ThumbnailMode;

import rxreddit.model.Link;

public interface LinkPresenter extends BasePresenter {

  void openLink(@NonNull Link link);

  void replyToLink(Link link);

  void upvoteLink(@NonNull Link link);

  void downvoteLink(@NonNull Link link);

  void showCommentsForLink(@NonNull Link link);

  void saveLink(@NonNull Link link);

  void unsaveLink(@NonNull Link link);

  void shareLink(@NonNull Link link);

  void openLinkSubreddit(@NonNull Link link);

  void openLinkUserProfile(@NonNull Link link);

  void openLinkInBrowser(@NonNull Link link);

  void openCommentsInBrowser(@NonNull Link link);

  void hideLink(@NonNull Link link);

  void unhideLink(@NonNull Link link);

  void reportLink(@NonNull Link link);

  boolean shouldShowNsfwTag();

  ThumbnailMode getThumbnailMode();
}
