package com.ddiehl.android.simpleredditreader.view.fragments;

import android.app.Activity;

import com.ddiehl.android.simpleredditreader.R;

public abstract class AbsUserProfileFragment extends AbsRedditFragment {

    @Override
    public void onAttach(Activity a) {
        super.onAttach(a);
        a.setTitle(R.string.user_profile_title);
    }
}
