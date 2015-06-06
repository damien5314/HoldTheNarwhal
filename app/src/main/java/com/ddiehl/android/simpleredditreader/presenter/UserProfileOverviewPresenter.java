package com.ddiehl.android.simpleredditreader.presenter;

import android.content.Context;

import com.ddiehl.android.simpleredditreader.view.ListingsView;

public class UserProfileOverviewPresenter extends AbsListingsPresenter {

    public UserProfileOverviewPresenter(Context context, ListingsView view, String username) {
        super(context, view, username, null, null, null, null, null);
    }

    @Override
    public void refreshData() {

    }

    @Override
    public void getMoreData() {

    }
}
