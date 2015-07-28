/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.presenter;

import android.content.Context;

import com.ddiehl.android.htn.events.requests.LoadUserProfileSummaryEvent;
import com.ddiehl.android.htn.view.ListingsView;

public class UserProfileSummaryPresenter extends AbsListingsPresenter {

    public UserProfileSummaryPresenter(Context c, ListingsView view, String show, String username,
                                       String subreddit, String sort, String timespan) {
        super(c, view, show, username, subreddit, sort, timespan);
    }

    @Override
    void requestData() {
        mBus.post(new LoadUserProfileSummaryEvent(mUsernameContext));
    }
}
