package com.ddiehl.android.simpleredditreader.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.presenter.UserProfilePresenter;
import com.ddiehl.android.simpleredditreader.view.ListingsView;
import com.ddiehl.android.simpleredditreader.view.adapters.ListingsAdapter;

import butterknife.ButterKnife;

public class UserProfileFragment extends AbsListingsFragment implements ListingsView {
    private static final String TAG = UserProfileFragment.class.getSimpleName();

    private static final String ARG_SHOW = "arg_show";
    private static final String ARG_USERNAME = "arg_username";

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
//        View v = inflater.inflate(R.layout.listings_user_profile_fragment, container, false);
        View v = inflater.inflate(R.layout.listings_fragment, container, false);
        ButterKnife.inject(this, v);
        instantiateListView(v);
        updateTitle();
        return v;
    }

    public void showUserProfile(String show, String username) {
        ((UserProfilePresenter) mListingsPresenter).requestData(show, username);
    }

    @Override
    public void updateTitle() {
        setTitle(String.format(getString(R.string.username), mListingsPresenter.getUsername()));
    }
}
