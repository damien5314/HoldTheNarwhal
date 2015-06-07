package com.ddiehl.android.simpleredditreader.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.presenter.UserProfileOverviewPresenter;
import com.ddiehl.android.simpleredditreader.view.ListingsView;
import com.ddiehl.android.simpleredditreader.view.adapters.ListingsAdapter;

public class UserProfileOverviewFragment extends AbsUserProfileFragment implements ListingsView {
    private static final String TAG = UserProfileOverviewFragment.class.getSimpleName();

    private static final int REQUEST_CHOOSE_SORT = 0;
    private static final String DIALOG_CHOOSE_SORT = "dialog_choose_sort";
    private static final String ARG_USERNAME = "arg_username";

    public UserProfileOverviewFragment() { }

    public static UserProfileOverviewFragment newInstance(String username) {
        UserProfileOverviewFragment f = new UserProfileOverviewFragment();
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
        mListingsPresenter = new UserProfileOverviewPresenter(getActivity(), this, username, "new", "all");
        mListingsAdapter = new ListingsAdapter(mListingsPresenter);
    }

    @Override
    public void updateTitle() {
        setTitle(String.format(getString(R.string.username), mListingsPresenter.getUsername()));
    }
}
