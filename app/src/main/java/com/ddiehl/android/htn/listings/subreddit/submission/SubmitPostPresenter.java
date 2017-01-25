package com.ddiehl.android.htn.listings.subreddit.submission;

import com.ddiehl.android.htn.HoldTheNarwhal;

import javax.inject.Inject;

import rx.Subscription;
import rxreddit.api.RedditService;

class SubmitPostPresenter {

    @Inject RedditService mRedditService;

    SubmitPostView mSubmitPostView;
    Subscription mSubmitSubscription;

    public SubmitPostPresenter() {
        HoldTheNarwhal.getApplicationComponent().inject(this);
    }

    public void saveSubmissionData() {
        // TODO
    }

    public void submit() {
        // TODO
    }

    public void cancelSubmit() {
        mSubmitSubscription.unsubscribe();
        mSubmitPostView.dismissAfterCancel();
    }
}
