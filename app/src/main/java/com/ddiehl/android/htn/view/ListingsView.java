/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view;

public interface ListingsView extends LinkView, CommentView {

    void displayOver18Required();
    void listingsUpdated();
    void listingUpdatedAt(int position);
    void listingRemovedAt(int position);

}
