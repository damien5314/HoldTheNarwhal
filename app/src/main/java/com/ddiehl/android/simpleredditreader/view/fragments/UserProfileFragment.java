package com.ddiehl.android.simpleredditreader.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.presenter.UserProfilePresenter;
import com.ddiehl.android.simpleredditreader.view.ListingsView;
import com.ddiehl.android.simpleredditreader.view.adapters.ListingsAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class UserProfileFragment extends AbsListingsFragment implements ListingsView {
    private static final String TAG = UserProfileFragment.class.getSimpleName();

    private static final String ARG_SHOW = "arg_show";
    private static final String ARG_USERNAME = "arg_username";

    @InjectView(R.id.navigation_tabs) TabLayout mTabLayout;

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

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.listings_user_profile_fragment, container, false);
        ButterKnife.inject(this, v);
        instantiateNavigationTabs();
        instantiateListView(v);
        updateTitle();
        return v;
    }

    public void instantiateNavigationTabs() {
        // Set up navigation tabs
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.navigation_tabs_overview)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.navigation_tabs_comments)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.navigation_tabs_submitted)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.navigation_tabs_gilded)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.navigation_tabs_upvoted)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.navigation_tabs_downvoted)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.navigation_tabs_hidden)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.navigation_tabs_saved)));
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        showUserProfile("overview");
                        break;
                    case 1:
                        showUserProfile("comments");
                        break;
                    case 2:
                        showUserProfile("submitted");
                        break;
                    case 3:
                        showUserProfile("gilded");
                        break;
                    case 4:
                        showUserProfile("upvoted");
                        break;
                    case 5:
                        showUserProfile("downvoted");
                        break;
                    case 6:
                        showUserProfile("hidden");
                        break;
                    case 7:
                        showUserProfile("saved");
                    default:
                        Log.w(TAG, "Unknown tab selected: " + tab.getPosition() + " " + tab.getText());
                }
            }
        });
    }

    @Override
    public void updateTitle() {
        setTitle(String.format(getString(R.string.username), mListingsPresenter.getUsername()));
    }
}
