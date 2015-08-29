/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.ddiehl.android.htn.BusProvider;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.events.requests.FriendAddEvent;
import com.ddiehl.android.htn.events.requests.FriendDeleteEvent;
import com.ddiehl.android.htn.events.requests.FriendNoteSaveEvent;
import com.ddiehl.android.htn.events.responses.FriendAddedEvent;
import com.ddiehl.android.htn.events.responses.FriendDeletedEvent;
import com.ddiehl.android.htn.events.responses.FriendInfoLoadedEvent;
import com.ddiehl.android.htn.events.responses.TrophiesLoadedEvent;
import com.ddiehl.android.htn.events.responses.UserInfoLoadedEvent;
import com.ddiehl.android.htn.presenter.UserProfilePresenter;
import com.ddiehl.android.htn.utils.BaseUtils;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.adapters.ListingsAdapter;
import com.ddiehl.reddit.identity.FriendInfo;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.Trophy;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UserProfileFragment extends AbsListingsFragment {

    private static final String ARG_SHOW = "arg_show";
    private static final String ARG_USERNAME = "arg_username";

    @Bind(R.id.user_profile_tabs) TabLayout mUserProfileTabs;
    @Bind(R.id.user_profile_summary) View mUserProfileSummary;
    @Bind(R.id.recycler_view) View mListView;
    @Bind(R.id.user_note_layout) View mFriendNoteLayout;

    // Views for user profile summary elements
    @Bind(R.id.user_created) TextView mCreateDate;
    @Bind(R.id.user_karma_layout) View mKarmaLayout;
    @Bind(R.id.user_link_karma) TextView mLinkKarma;
    @Bind(R.id.user_comment_karma) TextView mCommentKarma;
    @Bind(R.id.user_friend_button_layout) View mFriendButtonLayout;
    @Bind(R.id.user_friend_button) Button mFriendButton;
    @Bind(R.id.user_friend_note_edit) TextView mFriendNote;
    @Bind(R.id.user_friend_note_confirm) Button mFriendNoteSave;
    @Bind(R.id.user_trophies) LinearLayout mTrophies;

    private Context mContext;
    private Bus mBus = BusProvider.getInstance();

    public UserProfileFragment() { }

    public static UserProfileFragment newInstance(String show, String username) {
        UserProfileFragment f = new UserProfileFragment();
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
        mListingsPresenter = new UserProfilePresenter(getActivity(), this, show, username, "new", "all");
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
        mContext = v.getContext();
        ButterKnife.bind(this, v);
        instantiateListView(v);
        updateUserProfileTabs();
        mKarmaLayout.setVisibility(View.GONE);
        mFriendButtonLayout.setVisibility(View.GONE);
        mFriendNoteLayout.setVisibility(View.GONE);
        mFriendNoteSave.setOnClickListener((view) -> {
            showSpinner(null);
            String username = mListingsPresenter.getUsernameContext();
            String note = mFriendNote.getText().toString();
            mBus.post(new FriendNoteSaveEvent(username, note));
        });
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        mBus.register(this);
    }

    @Override
    public void onStop() {
        mBus.unregister(this);
        super.onStop();
    }

    @Subscribe
    public void onUserInfoLoaded(UserInfoLoadedEvent event) {
        dismissSpinner();
        if (event.isFailed()) {
            return;
        }

        UserIdentity user = event.getUserIdentity();
        String created = String.format(mContext.getString(R.string.user_profile_summary_created),
                SimpleDateFormat.getDateInstance().format(new Date(user.getCreatedUTC() * 1000)));
        mCreateDate.setText(created);
        mKarmaLayout.setVisibility(View.VISIBLE);
        mLinkKarma.setText(NumberFormat.getInstance().format(user.getLinkKarma()));
        mCommentKarma.setText(NumberFormat.getInstance().format(user.getCommentKarma()));

        // If user is not self, show friend button
        UserIdentity self = IdentityManager.getInstance(mContext).getUserIdentity();
        if (!user.getName().equals(self.getName())) {
            mFriendButtonLayout.setVisibility(View.VISIBLE);
            if (user.isFriend()) {
                setFriendButtonState(true);
            } else {
                setFriendButtonState(false);
            }
        }
    }

    @Subscribe
    public void onFriendInfoLoaded(FriendInfoLoadedEvent event) {
        dismissSpinner();
        if (event.isFailed()) {
            return;
        }

        UserIdentity self = IdentityManager.getInstance(mContext).getUserIdentity();
        if (self != null && self.isGold()) {
            mFriendNoteLayout.setVisibility(View.VISIBLE);
            FriendInfo friend = event.getFriendInfo();
            mFriendNote.setText(friend.getNote());
        }
    }

    @Subscribe
    public void onTrophiesLoaded(TrophiesLoadedEvent event) {
        dismissSpinner();
        if (event.isFailed()) {
            return;
        }

        final int minDpWidth = 160;
        final int numColumns = ((int) BaseUtils.getScreenWidth(mContext)) / minDpWidth;
        List<Listing> trophies = event.getListings();
        LinearLayout row = null;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mTrophies.removeAllViews(); // Reset layout
        for (int i = 0; i < trophies.size(); i++) {
            if (i % numColumns == 0) {
                row = (LinearLayout) inflater.inflate(R.layout.trophy_row, mTrophies, false);
                mTrophies.addView(row);
            }

            Trophy trophy = (Trophy) trophies.get(i);

            String name = trophy.getName();
            String description = trophy.getDescription();
            if (description != null) {
                name += "\n" + description;
            }

            LinearLayout v = (LinearLayout) inflater.inflate(R.layout.trophy_layout, row, false);
            if (row != null) row.addView(v);

            TextView trophyNameView = (TextView) v.findViewById(R.id.trophy_name);
            trophyNameView.setText(name);
            Picasso.with(mContext)
                    .load(trophy.getIcon70())
                    .into(((ImageView) v.findViewById(R.id.trophy_icon)));
        }
        int lastIndex = mTrophies.getChildCount() - 1;
        ((TableLayout.LayoutParams) mTrophies.getChildAt(lastIndex).getLayoutParams())
                .bottomMargin = 0;
    }

    @Subscribe
    public void onFriendAdded(FriendAddedEvent event) {
        dismissSpinner();
        if (event.isFailed()) {
            showToast(R.string.user_friend_add_error);
            return;
        }
        setFriendButtonState(true);

        UserIdentity self = IdentityManager.getInstance(mContext).getUserIdentity();
        if (self != null && self.isGold()) {
            mFriendNote.setText(event.getNote());
            mFriendNoteLayout.setVisibility(View.VISIBLE);
        }
    }

    @Subscribe
    public void onFriendDeleted(FriendDeletedEvent event) {
        dismissSpinner();
        if (event.isFailed()) {
            return;
        }
        setFriendButtonState(false);
        mFriendNote.setText("");
        mFriendNoteLayout.setVisibility(View.GONE);
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
//                    ((MainView) getActivity()).showUserProfile(tag, mListingsPresenter.getUsernameContext());
                } else {
                    mUserProfileSummary.setVisibility(View.GONE);
                    mListView.setVisibility(View.VISIBLE);
//                    ((UserProfileListingPresenter) mListingsPresenter).requestData(tag);
                }

                ((UserProfilePresenter) mListingsPresenter).requestData(tag);
            }
        });
    }

    private void setFriendButtonState(boolean isFriend) {
        String username = mListingsPresenter.getUsernameContext();
        if (isFriend) {
            mFriendButton.setText(R.string.user_friend_delete_button_text);
            mFriendButton.setOnClickListener((v) -> {
                ((MainView) getActivity()).showSpinner(null);
                mBus.post(new FriendDeleteEvent(username));
            });
        } else {
            mFriendButton.setText(R.string.user_friend_add_button_text);
            mFriendButton.setOnClickListener((v) -> {
                ((MainView) getActivity()).showSpinner(null);
                mBus.post(new FriendAddEvent(username));
            });
        }
    }

    @Override
    public void updateTitle() {
        setTitle(String.format(getString(R.string.username), mListingsPresenter.getUsernameContext()));
    }

    @Override
    public void showSpinner(String msg) {
        if (mListingsPresenter.getShow().equals("summary")) {
            ((MainView) getActivity()).showSpinner(msg);
            return;
        }
        mSwipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void showSpinner(int resId) {
        this.showSpinner(getString(resId));
    }

    @Override
    public void dismissSpinner() {
        if (mListingsPresenter.getShow().equals("summary")) {
            ((MainView) getActivity()).dismissSpinner();
            return;
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }
}
