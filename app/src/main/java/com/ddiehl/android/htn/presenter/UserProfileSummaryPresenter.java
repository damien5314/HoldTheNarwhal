/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.presenter;

import android.content.Context;

import com.ddiehl.android.htn.events.requests.LoadUserProfileSummaryEvent;

public class UserProfileSummaryPresenter extends AbsListingsPresenter {

    public UserProfileSummaryPresenter(Context c, String username) {
        super(c, null, null, username, null, null, null);
    }

    @Override
    public void refreshData() {
        if (mListingsRequested)
            return;

        mListingsRequested = true;
        mListingsView.showSpinner(null);
        requestData();
    }

    @Override
    void requestData() {
        mBus.post(new LoadUserProfileSummaryEvent(mUsernameContext));
    }

}
