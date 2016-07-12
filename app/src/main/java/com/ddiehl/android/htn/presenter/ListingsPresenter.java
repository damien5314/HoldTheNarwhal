package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;

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

  boolean hasPreviousListings();

  boolean hasNextListings();

  boolean getShowControversiality();

  void onSortChanged();

  UserIdentity getAuthorizedUser();
}
