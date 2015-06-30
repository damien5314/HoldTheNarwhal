/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view.fragments;

import android.app.Fragment;

import com.ddiehl.android.htn.view.MainView;

public abstract class AbsRedditFragment extends Fragment {

    public void setTitle(CharSequence title) {
        getActivity().setTitle(title);
    }

    public void showSpinner(String msg) {
        ((MainView) getActivity()).showSpinner(msg);
    }

    public void showSpinner(int resId) {
        ((MainView) getActivity()).showSpinner(resId);
    }

    public void dismissSpinner() {
        ((MainView) getActivity()).dismissSpinner();
    }

    public void showToast(String msg) {
        ((MainView) getActivity()).showToast(msg);
    }

    public void showToast(int resId) {
        ((MainView) getActivity()).showToast(resId);
    }

}
