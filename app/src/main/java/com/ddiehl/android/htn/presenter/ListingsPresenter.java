/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.Listing;

import java.util.List;

public interface ListingsPresenter extends BasePresenter, LinkPresenter, CommentPresenter {

    void refreshData();
    void setData(@NonNull List<Listing> data);
    void getMoreData();

    int getNumListings();
    Listing getListing(int position);

    String getUsernameContext();
    String getSubreddit();
    String getNextPageListingId();
    String getSort();
    String getTimespan();
    String getShow();
    boolean getShowControversiality();
    void updateSubreddit(@Nullable String subreddit);
    void updateSort();
    void updateSort(@Nullable String sort);
    void updateSort(@Nullable String sort, @Nullable String timespan);

    UserIdentity getAuthorizedUser();
    boolean dataRequested();
}
