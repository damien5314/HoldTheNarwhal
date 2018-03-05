package com.ddiehl.android.htn.subscriptions;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.subredditinfo.InfoTuple;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import rxreddit.api.RedditService;
import rxreddit.model.ListingResponse;
import rxreddit.model.Subreddit;

public class SubscriptionManagerPresenter {

    @Inject RedditService redditService;

    public SubscriptionManagerPresenter() {
        HoldTheNarwhal.getApplicationComponent().inject(this);
    }

    /**
     * Get list of {@link rxreddit.model.Subreddit} to which user is subscribed.
     * @param where Valid: "subscriber", "contributor", "moderator"
     */
    public Observable<ListingResponse> getSubscriptions(
            @NotNull String where, @Nullable String beforePageId, @Nullable String nextPageId) {
        return redditService.getSubreddits(where, beforePageId, nextPageId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable subscribe(Subreddit subreddit) {
        String name = subreddit.getDisplayName();
        return redditService.subscribe(name);
    }

    public Completable unsubscribe(Subreddit subreddit) {
        String name = subreddit.getDisplayName();
        return redditService.unsubscribe(name);
    }

    public Observable<InfoTuple> getSubredditInfo(final @NotNull String subreddit) {
        return Observable.combineLatest(
                redditService.getSubredditInfo(subreddit),
                redditService.getSubredditRules(subreddit),
//                redditService.getSubredditSidebar(subreddit),
                (subreddit2, rules) -> {
                    InfoTuple tuple = new InfoTuple();
                    tuple.subreddit = subreddit2;
                    tuple.rules = rules;
                    return tuple;
                }
        );
    }
}
