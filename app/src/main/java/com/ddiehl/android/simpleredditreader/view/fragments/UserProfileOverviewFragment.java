package com.ddiehl.android.simpleredditreader.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.view.UserOverviewView;

public class UserProfileOverviewFragment extends UserProfileFragment implements UserOverviewView {
    private static final String TAG = UserProfileOverviewFragment.class.getSimpleName();

    private static final String ARG_USER_ID = "arg_user_id";

    private String mUserId;

    public UserProfileOverviewFragment() { }

    public static UserProfileOverviewFragment newInstance(String userId) {
        UserProfileOverviewFragment f = new UserProfileOverviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mUserId = args.getString(ARG_USER_ID);
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v  = inflater.inflate(R.layout.user_profile_overview, container, false);
        return v;
    }
}
