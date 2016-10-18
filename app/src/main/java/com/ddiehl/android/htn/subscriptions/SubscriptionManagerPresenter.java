package com.ddiehl.android.htn.subscriptions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ddiehl.android.htn.HoldTheNarwhal;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rxreddit.api.RedditService;
import rxreddit.model.ListingResponse;
import rxreddit.model.Subreddit;

public class SubscriptionManagerPresenter {

    @Inject RedditService mRedditService;

    public SubscriptionManagerPresenter() {
        HoldTheNarwhal.getApplicationComponent().inject(this);
    }

    /**
     * Get list of {@link rxreddit.model.Subreddit} to which user is subscribed.
     * @param where Valid: "subscriber", "contributor", "moderator"
     */
    public Observable<ListingResponse> getSubscriptions(
            @NonNull String where, @Nullable String beforePageId, @Nullable String nextPageId) {
        return mRedditService.getSubreddits(where, beforePageId, nextPageId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Void> subscribe(Subreddit subreddit) {
        String name = subreddit.getName();
        return mRedditService.subscribe(name);
    }

    public Observable<Void> unsubscribe(Subreddit subreddit) {
        String name = subreddit.getName();
        return mRedditService.unsubscribe(name);
    }
}
