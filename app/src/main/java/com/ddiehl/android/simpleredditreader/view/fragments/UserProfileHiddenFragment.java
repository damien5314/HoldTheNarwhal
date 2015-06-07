package com.ddiehl.android.simpleredditreader.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.presenter.UserProfileHidePresenter;
import com.ddiehl.android.simpleredditreader.view.ListingsView;
import com.ddiehl.android.simpleredditreader.view.adapters.ListingsAdapter;

public class UserProfileHiddenFragment extends AbsUserProfileFragment implements ListingsView {

    private static final String ARG_USERNAME = "arg_username";

    public UserProfileHiddenFragment() { }

    public static UserProfileHiddenFragment newInstance(String username) {
        UserProfileHiddenFragment f = new UserProfileHiddenFragment();
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
        mListingsPresenter = new UserProfileHidePresenter(getActivity(), this, username, "new", "all");
        mListingsAdapter = new ListingsAdapter(mListingsPresenter);
    }

    @Override
    public void updateTitle() {
        setTitle(String.format(getString(R.string.username), mListingsPresenter.getUsername()));
    }
}
