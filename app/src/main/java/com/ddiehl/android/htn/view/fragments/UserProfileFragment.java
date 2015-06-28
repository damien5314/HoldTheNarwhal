package com.ddiehl.android.htn.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.UserProfilePresenter;
import com.ddiehl.android.htn.view.ListingsView;
import com.ddiehl.android.htn.view.adapters.ListingsAdapter;
import com.ddiehl.reddit.identity.UserIdentity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UserProfileFragment extends AbsListingsFragment implements ListingsView {

    private static final String ARG_SHOW = "arg_show";
    private static final String ARG_USERNAME = "arg_username";

    @Bind(R.id.user_profile_tabs) TabLayout mUserProfileTabs;

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
//        View v = inflater.inflate(R.layout.listings_fragment, container, false);
        View v = inflater.inflate(R.layout.listings_user_profile_fragment, container, false);
        ButterKnife.bind(this, v);
        instantiateListView(v);
        updateUserProfileTabs();
//        updateTitle();
        return v;
    }

    public void updateUserProfileTabs() {
        mUserProfileTabs.removeAllTabs();
        mUserProfileTabs.setOnTabSelectedListener(null);

        // Normal tabs
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
//                showUserProfile((String) tab.getTag(), mMainPresenter.getUsernameContext());
                ((UserProfilePresenter) mListingsPresenter).requestData((String) tab.getTag());
            }
        });
    }

    @Override
    public void updateTitle() {
        setTitle(String.format(getString(R.string.username), mListingsPresenter.getUsernameContext()));
    }
}
