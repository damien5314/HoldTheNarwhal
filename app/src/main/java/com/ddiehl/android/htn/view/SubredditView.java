package com.ddiehl.android.htn.view;

public interface SubredditView extends ListingsView, LinkView {

  String getSubreddit();

  String getSort();

  String getTimespan();

  void showNsfwWarningDialog();
}
