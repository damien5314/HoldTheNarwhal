package com.ddiehl.android.simpleredditreader.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.presenter.UserProfileDownvotedPresenter;
import com.ddiehl.android.simpleredditreader.view.ListingsView;
import com.ddiehl.android.simpleredditreader.view.adapters.ListingsAdapter;

public class UserProfileDownvotedFragment extends AbsUserProfileFragment implements ListingsView {

    private static final String ARG_USERNAME = "arg_username";

    public UserProfileDownvotedFragment() { }

    public static UserProfileDownvotedFragment newInstance(String username) {
        UserProfileDownvotedFragment f = new UserProfileDownvotedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USERNAME, username);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        String username = args.getString(ARG_USERNAME);
        mListingsPresenter = new UserProfileDownvotedPresenter(getActivity(), this, username, "new", "all");
        mListingsAdapter = new ListingsAdapter(mListingsPresenter);
    }

    @Override
    public void updateTitle() {
        setTitle(String.format(getString(R.string.username), mListingsPresenter.getUsername()));
    }
}
