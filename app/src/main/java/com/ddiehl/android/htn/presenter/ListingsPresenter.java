/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.presenter;

import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.Listing;

import java.util.List;

public interface ListingsPresenter extends LinkPresenter, CommentPresenter {

    void refreshData();
    void setData(List<Listing> data);
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
    void updateSubreddit(String subreddit);
    void updateSort();
    void updateSort(String sort);
    void updateSort(String sort, String timespan);

    UserIdentity getAuthorizedUser();
}
