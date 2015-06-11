package com.ddiehl.android.simpleredditreader.presenter;

import com.ddiehl.reddit.listings.AbsRedditComment;
import com.ddiehl.reddit.listings.RedditLink;
import com.ddiehl.reddit.listings.RedditMoreComments;

public interface LinkCommentsPresenter extends LinkPresenter, CommentPresenter {

    RedditLink getLinkContext();
    void getComments();
    void getMoreChildren(RedditMoreComments comment);
    void toggleThreadVisible(AbsRedditComment comment);

    int getNumComments();
    AbsRedditComment getComment(int position);

    String getSort();
    void updateSort();
    void updateSort(String sort);

}
