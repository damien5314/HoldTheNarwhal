package com.ddiehl.android.simpleredditreader.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.presenter.UserProfileSavedPresenter;
import com.ddiehl.android.simpleredditreader.view.ListingsView;
import com.ddiehl.android.simpleredditreader.view.adapters.ListingsAdapter;

public class UserProfileSavedFragment extends AbsUserProfileFragment implements ListingsView {

    private static final String ARG_USERNAME = "arg_username";

    public UserProfileSavedFragment() { }

    public static UserProfileSavedFragment newInstance(String username) {
        UserProfileSavedFragment f = new UserProfileSavedFragment();
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
        mListingsPresenter = new UserProfileSavedPresenter(getActivity(), this, username, "new", "all");
        mListingsAdapter = new ListingsAdapter(mListingsPresenter);
    }

    @Override
    public void updateTitle() {
        setTitle(String.format(getString(R.string.username), mListingsPresenter.getUsername()));
    }
}
