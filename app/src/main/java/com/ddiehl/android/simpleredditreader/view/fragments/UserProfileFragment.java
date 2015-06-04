package com.ddiehl.android.simpleredditreader.view.fragments;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.ddiehl.android.simpleredditreader.R;

public abstract class UserProfileFragment extends Fragment {

    @Override
    public void onAttach(Activity a) {
        super.onAttach(a);
        a.setTitle(R.string.user_profile_title);
    }
}
