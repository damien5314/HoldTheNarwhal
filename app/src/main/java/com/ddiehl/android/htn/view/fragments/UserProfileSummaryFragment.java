/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.UserProfileSummaryPresenter;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.reddit.identity.UserIdentity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UserProfileSummaryFragment extends AbsRedditFragment {

    private static final String ARG_USERNAME = "arg_username";

    private UserProfileSummaryPresenter mPresenter;

    @Bind(R.id.user_profile_tabs) TabLayout mUserProfileTabs;

    public UserProfileSummaryFragment() { }

    public static UserProfileListingFragment newInstance(String username) {
        UserProfileListingFragment f = new UserProfileListingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USERNAME, username);
        f.setArguments(args);
        return f;
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.listings_user_profile_fragment, container, false);
        ButterKnife.bind(this, v);
        instantiateView();
        updateUserProfileTabs();
        return v;
    }

    private void instantiateView() {

    }

    public void updateUserProfileTabs() {
        mUserProfileTabs.removeAllTabs();
        mUserProfileTabs.setOnTabSelectedListener(null);

        // Normal tabs
        mUserProfileTabs.addTab(mUserProfileTabs.newTab()
                .setText(getString(R.string.navigation_tabs_summary)).setTag("summary"));
        mUserProfileTabs.addTab(mUserProfileTabs.newTab()
                .setText(getString(R.string.navigation_tabs_overview)).setTag("overview"));
        mUserProfileTabs.addTab(mUserProfileTabs.newTab()
                .setText(getString(R.string.navigation_tabs_comments)).setTag("comments"));
        mUserProfileTabs.addTab(mUserProfileTabs.newTab()
                .setText(getString(R.string.navigation_tabs_submitted)).setTag("submitted"));
        mUserProfileTabs.addTab(mUserProfileTabs.newTab()
                .setText(getString(R.string.navigation_tabs_gilded)).setTag("gilded"));

        // Authorized tabs
        UserIdentity id = mPresenter.getAuthorizedUser();
        boolean showAuthorizedTabs = id != null &&
                id.getName().equals(mPresenter.getUsernameContext());
        if (showAuthorizedTabs) {
            mUserProfileTabs.addTab(mUserProfileTabs.newTab()
                    .setText(getString(R.string.navigation_tabs_upvoted)).setTag("upvoted"));
            mUserProfileTabs.addTab(mUserProfileTabs.newTab()
                    .setText(getString(R.string.navigation_tabs_downvoted)).setTag("downvoted"));
            mUserProfileTabs.addTab(mUserProfileTabs.newTab()
                    .setText(getString(R.string.navigation_tabs_hidden)).setTag("hidden"));
            mUserProfileTabs.addTab(mUserProfileTabs.newTab()
                    .setText(getString(R.string.navigation_tabs_saved)).setTag("saved"));
        }

        mUserProfileTabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getTag().equals("summary")) {
                    // Already on this tab
                } else {
                    ((MainView) getActivity()).showUserProfile(
                            (String) tab.getTag(), mPresenter.getUsernameContext());
                }
            }
        });
    }

    @Override
    public void updateTitle() {
        setTitle(String.format(getString(R.string.username), mPresenter.getUsernameContext()));
    }


}
