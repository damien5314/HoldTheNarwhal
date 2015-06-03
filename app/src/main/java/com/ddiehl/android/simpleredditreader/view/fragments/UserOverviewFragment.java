package com.ddiehl.android.simpleredditreader.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.view.UserOverviewView;

public class UserOverviewFragment extends Fragment implements UserOverviewView {
    private static final String TAG = UserOverviewFragment.class.getSimpleName();

    private static final String ARG_ID = "arg_id";

    private String mId;

    public UserOverviewFragment() { }

    public static UserOverviewFragment newInstance(String id) {
        UserOverviewFragment f = new UserOverviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID, id);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mId = args.getString(ARG_ID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v  = inflater.inflate(R.layout.user_profile_overview, container, false);
        return v;
    }
}
