package com.ddiehl.android.htn.subscriptions;

import com.ddiehl.android.htn.HoldTheNarwhal;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rxreddit.api.RedditService;
import rxreddit.model.ListingResponse;

public class SubscriptionManagerPresenter {

    @Inject RedditService mRedditService;

    public SubscriptionManagerPresenter() {
        HoldTheNarwhal.getApplicationComponent().inject(this);
    }

    public Observable<ListingResponse> getSubscriptions() {
        return mRedditService.getSubscriberSubreddits()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
