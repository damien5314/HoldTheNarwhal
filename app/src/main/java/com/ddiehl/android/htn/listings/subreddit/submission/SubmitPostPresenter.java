package com.ddiehl.android.htn.listings.subreddit.submission;

import com.ddiehl.android.htn.HoldTheNarwhal;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import rxreddit.api.RedditService;

public class SubmitPostPresenter {

    @Inject RedditService mRedditService;

    SubmitPostView mSubmitPostView;
    Disposable mSubmitSubscription;

    public SubmitPostPresenter(SubmitPostView view) {
        HoldTheNarwhal.getApplicationComponent().inject(this);
        mSubmitPostView = view;
    }

    public void saveSubmissionData() {
        // TODO
    }

    public void submit(@NotNull String kind) {
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
        mSubmitSubscription.dispose();
        mSubmitPostView.dismissAfterCancel();
    }
}
