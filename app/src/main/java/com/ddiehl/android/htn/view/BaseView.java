/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view;

public interface BaseView {

    void setTitle(CharSequence title);
    void showSpinner(String msg);
    void showSpinner(int resId);
    void dismissSpinner();
    void showToast(String msg);
    void showToast(int resId);

    void loadNavigationDrawerImage(String url);
}
