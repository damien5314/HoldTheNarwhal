package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;

import rxreddit.model.Comment;
import rxreddit.model.CommentStub;

public interface CommentPresenter extends BasePresenter {

    void showCommentThread(@NonNull String subreddit, @NonNull String linkId, @NonNull String commentId);

    void getMoreComments(@NonNull CommentStub comment);

    void openCommentPermalink(@NonNull Comment comment);

    void replyToComment(@NonNull Comment comment);

    void upvoteComment(@NonNull Comment comment);

    void downvoteComment(@NonNull Comment comment);

    void saveComment(@NonNull Comment comment);

    void unsaveComment(@NonNull Comment comment);

    void shareComment(@NonNull Comment link);

    void openCommentUserProfile(@NonNull Comment comment);

    void openCommentInBrowser(@NonNull Comment comment);

    void reportComment(@NonNull Comment comment);

    void openCommentLink(@NonNull Comment comment);
}
