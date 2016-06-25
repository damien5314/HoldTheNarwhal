package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import rxreddit.model.Listing;
import rxreddit.model.UserIdentity;

public interface ListingsPresenter extends BasePresenter {
  void refreshData();
  void setData(@NonNull List<Listing> data);
  void getPreviousData();
  void getNextData();
  int getNumListings();
  Listing getListingAt(int position);
  String getUsernameContext();
  String getSubreddit();
  boolean hasPreviousListings();
  boolean hasNextListings();
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
