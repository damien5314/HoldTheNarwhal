package com.ddiehl.android.htn.presenter;

import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.reddit.listings.RedditComment;
import com.ddiehl.reddit.listings.RedditMoreComments;

public interface CommentPresenter {

    void showCommentContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, RedditComment comment);
    void showCommentThread(String subreddit, String linkId, String commentId);
    void getMoreChildren(RedditMoreComments comment);
    void openCommentPermalink();
    void openReplyView();
    void upvoteComment();
    void downvoteComment();
    void saveComment();
    void unsaveComment();
    void shareComment();
    void openCommentUserProfile();
    void openCommentUserProfile(RedditComment comment);
    void openCommentInBrowser();
    void reportComment();
    void openCommentLink(RedditComment comment);

}
