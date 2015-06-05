package com.ddiehl.android.simpleredditreader.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.simpleredditreader.R;

public class UserProfileCommentsFragment extends AbsUserProfileFragment {
    private static final String TAG = UserProfileCommentsFragment.class.getSimpleName();

    private static final String ARG_USER_ID = "arg_user_id";

    private String mUserId;

    public UserProfileCommentsFragment() { }

    public static UserProfileCommentsFragment newInstance(String userId) {
        UserProfileCommentsFragment f = new UserProfileCommentsFragment();
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
        View v  = inflater.inflate(R.layout.user_profile_comments, container, false);
        return v;
    }
}
