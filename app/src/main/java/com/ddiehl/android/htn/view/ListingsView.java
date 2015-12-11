package com.ddiehl.android.htn.view;

public interface ListingsView extends LinkView, CommentView {

  void listingsUpdated();
  void listingUpdatedAt(int position);
  void listingRemovedAt(int position);

  void showSortOptionsMenu();
  void showTimespanOptionsMenu();
  void onSortChanged();
  void onTimespanChanged();
  void goBack();
}
