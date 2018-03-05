package com.ddiehl.android.htn.listings.subreddit.submission;

import com.ddiehl.android.htn.HoldTheNarwhal;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import rxreddit.api.RedditService;

public class SubmitPostPresenter {

    @Inject RedditService redditService;

    SubmitPostView submitPostView;
    Disposable submitSubscription;

    public SubmitPostPresenter(SubmitPostView view) {
        HoldTheNarwhal.getApplicationComponent().inject(this);
        submitPostView = view;
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
        redditService.submit(
                submitPostView.getSubreddit(),
                "link",
                submitPostView.getTitle(),
                submitPostView.getUrl(),
                null, // Text is null for link submissions
                submitPostView.getSendReplies(),
                false
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(submitPostView::onPostSubmitted, submitPostView::onSubmitError);
    }

    void submitText() {
        redditService.submit(
                submitPostView.getSubreddit(),
                "self",
                submitPostView.getTitle(),
                null, // URL is null for text submissions
                submitPostView.getText(),
                submitPostView.getSendReplies(),
                false
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(submitPostView::onPostSubmitted, submitPostView::onSubmitError);
    }

    public void cancelSubmit() {
        submitSubscription.dispose();
        submitPostView.dismissAfterCancel();
    }
}
