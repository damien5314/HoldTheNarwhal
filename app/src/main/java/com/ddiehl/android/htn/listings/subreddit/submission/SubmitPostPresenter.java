package com.ddiehl.android.htn.listings.subreddit.submission;

import android.support.annotation.NonNull;

import com.ddiehl.android.htn.HoldTheNarwhal;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rxreddit.api.RedditService;

public class SubmitPostPresenter {

    @Inject RedditService mRedditService;

    SubmitPostView mSubmitPostView;
    Subscription mSubmitSubscription;

    public SubmitPostPresenter(SubmitPostView view) {
        HoldTheNarwhal.getApplicationComponent().inject(this);
        mSubmitPostView = view;
    }

    public void saveSubmissionData() {
        // TODO
    }

    public void submit(@NonNull String kind) {
        if ("link".equals(kind)) {
            submitLink();
        } else if ("self".equals(kind)) {
            submitText();
        }
    }

    void submitLink() {
        mRedditService.submit(
                mSubmitPostView.getSubreddit(),
                "link",
                mSubmitPostView.getTitle(),
                mSubmitPostView.getUrl(),
                null, // Text is null for link submissions
                mSubmitPostView.getSendReplies(),
                false
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mSubmitPostView::onPostSubmitted, mSubmitPostView::onSubmitError);
    }

    void submitText() {
        mRedditService.submit(
                mSubmitPostView.getSubreddit(),
                "self",
                mSubmitPostView.getTitle(),
                null, // URL is null for text submissions
                mSubmitPostView.getText(),
                mSubmitPostView.getSendReplies(),
                false
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mSubmitPostView::onPostSubmitted, mSubmitPostView::onSubmitError);
    }

    public void cancelSubmit() {
        mSubmitSubscription.unsubscribe();
        mSubmitPostView.dismissAfterCancel();
    }
}
