package com.ddiehl.android.simpleredditreader.presenter;

import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;

import com.ddiehl.reddit.listings.AbsRedditComment;
import com.ddiehl.reddit.listings.RedditComment;
import com.ddiehl.reddit.listings.RedditLink;
import com.ddiehl.reddit.listings.RedditMoreComments;

public interface CommentsPresenter {

    void getComments();
    RedditLink getLink();
    void setLink(RedditLink link);
    void showMoreChildren(RedditMoreComments comment);
    void updateSort(String sort);
    AbsRedditComment getCommentAtPosition(int position);
    void toggleThreadVisible(AbsRedditComment comment);
    int getNumComments();
    void createContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, RedditComment redditComment);
    boolean onContextItemSelected(MenuItem item);
    void navigateToCommentThread(RedditMoreComments comment);
}
