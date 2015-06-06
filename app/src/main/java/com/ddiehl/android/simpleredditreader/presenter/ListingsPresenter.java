package com.ddiehl.android.simpleredditreader.presenter;

import com.ddiehl.reddit.listings.Listing;

import java.util.List;

public interface ListingsPresenter extends LinkPresenter, CommentPresenter {

    void refreshData();
    void setData(List<Listing> data);
    void getMoreData();

    int getNumListings();
    Listing getListing(int position);

    String getSubreddit();
    String getSort();
    String getTimespan();
    void updateSubreddit(String subreddit);
    void updateSort();
    void updateSort(String sort);
    void updateSort(String sort, String timespan);

}
