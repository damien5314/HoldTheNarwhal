package com.ddiehl.android.simpleredditreader.view;

public interface CommentsView {

    void setTitle(String title);
    void updateAdapter();
    void showSpinner(String msg);
    void dismissSpinner();
}
