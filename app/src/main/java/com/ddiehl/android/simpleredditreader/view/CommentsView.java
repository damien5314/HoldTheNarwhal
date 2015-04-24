package com.ddiehl.android.simpleredditreader.view;

import com.ddiehl.android.simpleredditreader.model.listings.CommentBank;
import com.ddiehl.android.simpleredditreader.model.listings.RedditLink;

public interface CommentsView {

    public void showSpinner(String msg);
    public void dismissSpinner();
    public boolean isViewVisible();
    public void setTitle(String title);
    public void updateAdapter();
    public void showLink(RedditLink link);
    public void showComments(CommentBank bank);

}
