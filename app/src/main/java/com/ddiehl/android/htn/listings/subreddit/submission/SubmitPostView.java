package com.ddiehl.android.htn.listings.subreddit.submission;

import rxreddit.model.SubmitPostResponse;

interface SubmitPostView {

    void dismissAfterConfirmation();

    void dismissAfterCancel();

    String getSubreddit();

    String getTitle();

    String getUrl();

    String getText();

    boolean getSendReplies();

    void onPostSubmitted(SubmitPostResponse result);

    void onSubmitError(Throwable error);
}
