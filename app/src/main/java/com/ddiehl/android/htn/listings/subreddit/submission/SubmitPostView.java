package com.ddiehl.android.htn.listings.subreddit.submission;

interface SubmitPostView {

    void dismissAfterConfirmation();

    void dismissAfterCancel();

    String getSubreddit();

    String getTitle();

    String getUrl();

    String getText();

    boolean getSendReplies();

    void onPostSubmitted(Void result);

    void onSubmitError(Throwable error);
}
