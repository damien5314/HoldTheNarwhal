package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.Listing;

import java.util.List;

public interface ListingsPresenter extends BasePresenter {
  void refreshData();
  void setData(@NonNull List<Listing> data);
  void getMoreData();
  void setSelectedListing(@NonNull Listing listing);
  int getNumListings();
  Listing getListing(int position);
  String getUsernameContext();
  String getSubreddit();
  String getNextPageListingId();
  String getSort();
  String getTimespan();
  String getShow();
  boolean getShowControversiality();
  void onSortChanged();
  UserIdentity getAuthorizedUser();
  void onSortSelected(@Nullable String sort);
  void onTimespanSelected(@Nullable String timespan);
  void onNsfwSelected(boolean nsfwAllowed);
}
