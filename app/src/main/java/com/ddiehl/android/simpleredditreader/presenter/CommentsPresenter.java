package com.ddiehl.android.simpleredditreader.presenter;

import com.ddiehl.reddit.listings.AbsRedditComment;
import com.ddiehl.reddit.listings.RedditLink;
import com.ddiehl.reddit.listings.RedditMoreComments;

public interface CommentsPresenter {

    void getComments();
    RedditLink getLink();
    void setLink(RedditLink link);
    void getMoreChildren(RedditMoreComments comment);
    void updateSort(String sort);
    AbsRedditComment getCommentAtPosition(int position);
    void toggleThreadVisible(AbsRedditComment comment);
    int getNumComments();
}
