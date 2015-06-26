package com.ddiehl.android.htn.view;

public interface ListingsView extends LinkView, CommentView {

    void listingsUpdated();
    void listingUpdatedAt(int position);
    void listingRemovedAt(int position);

}