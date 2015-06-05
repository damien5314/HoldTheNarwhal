package com.ddiehl.android.simpleredditreader.view;

public interface BaseView {

    void setTitle(CharSequence title);
    void showSpinner(String msg);
    void showSpinner(int resId);
    void dismissSpinner();
    void showToast(String msg);
    void showToast(int resId);

}
