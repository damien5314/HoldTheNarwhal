package com.ddiehl.android.simpleredditreader.presenter;

import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.reddit.listings.AbsRedditComment;
import com.ddiehl.reddit.listings.RedditComment;
import com.ddiehl.reddit.listings.RedditLink;
import com.ddiehl.reddit.listings.RedditMoreComments;

public interface CommentsPresenter {

    RedditLink getLink();
    void setLink(RedditLink link);
    void getComments();
    void showMoreChildren(RedditMoreComments comment);
    void toggleThreadVisible(AbsRedditComment comment);
    void updateSort(String sort);
    int getNumComments();
    AbsRedditComment getCommentAtPosition(int position);

    void showContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, RedditComment redditComment);
    void navigateToCommentThread(RedditMoreComments comment);
    void openReplyView();
    void upvote();
    void downvote();
    void saveComment();
    void unsaveComment();
    void shareComment();
    void openCommentInBrowser();
    void hideComment();
    void unhideComment();
    void reportComment();

}
