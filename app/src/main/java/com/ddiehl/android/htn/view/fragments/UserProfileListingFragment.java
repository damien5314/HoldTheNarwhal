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
import android.widget.GridLayout;
import android.widget.TextView;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.UserProfileListingPresenter;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.adapters.ListingsAdapter;
import com.ddiehl.reddit.identity.UserIdentity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UserProfileListingFragment extends AbsListingsFragment {

    private static final String ARG_SHOW = "arg_show";
    private static final String ARG_USERNAME = "arg_username";

    @Bind(R.id.user_profile_tabs) TabLayout mUserProfileTabs;
    @Bind(R.id.user_profile_summary) View mUserProfileSummary;
    @Bind(R.id.recycler_view) View mListView;
    @Bind(R.id.user_note_layout) View mUserNoteLayout;

    // Views for user profile summary elements
    @Bind(R.id.user_created) TextView mCreateDate;
    @Bind(R.id.user_link_karma) TextView mLinkKarma;
    @Bind(R.id.user_comment_karma) TextView mCommentKarma;
    @Bind(R.id.user_friend_note) TextView mFriendNote;
    @Bind(R.id.user_trophies) GridLayout mTrophies;

    public UserProfileListingFragment() { }

    public static UserProfileListingFragment newInstance(String show, String username) {
        UserProfileListingFragment f = new UserProfileListingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SHOW, show);
        args.putString(ARG_USERNAME, username);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        String show = args.getString(ARG_SHOW);
        String username = args.getString(ARG_USERNAME);
        mListingsPresenter = new UserProfileListingPresenter(getActivity(), this, show, username, "new", "all");
        mListingsAdapter = new ListingsAdapter(mListingsPresenter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateTitle();
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.user_profile_listings_fragment, container, false);
        ButterKnife.bind(this, v);
        instantiateListView(v);
        updateUserProfileTabs();
        return v;
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
        UserIdentity id = mListingsPresenter.getAuthorizedUser();
        boolean showAuthorizedTabs = id != null &&
                id.getName().equals(mListingsPresenter.getUsernameContext());
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
//                ((MainView) getActivity()).showUserProfile((String) tab.getTag(),
//                        mListingsPresenter.getUsernameContext());
                String tag = (String) tab.getTag();
                if (tab.getTag().equals("summary")) {
                    mUserProfileSummary.setVisibility(View.VISIBLE);
                    mListView.setVisibility(View.GONE);
                    ((MainView) getActivity()).showUserProfile(tag, mListingsPresenter.getUsernameContext());
                } else {
                    mUserProfileSummary.setVisibility(View.GONE);
                    mListView.setVisibility(View.VISIBLE);
                    ((UserProfileListingPresenter) mListingsPresenter).requestData(tag);
                }
            }
        });
    }

    @Override
    public void updateTitle() {
        setTitle(String.format(getString(R.string.username), mListingsPresenter.getUsernameContext()));
    }
}
