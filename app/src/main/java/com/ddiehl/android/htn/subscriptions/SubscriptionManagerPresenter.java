package com.ddiehl.android.htn.subscriptions;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.subredditinfo.InfoTuple;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import rxreddit.api.RedditService;
import rxreddit.model.ListingResponse;
import rxreddit.model.Subreddit;

public class SubscriptionManagerPresenter {

    @Inject
    RedditService redditService;

    public SubscriptionManagerPresenter() {
        HoldTheNarwhal.getApplicationComponent().inject(this);
    }

    /**
     * Get list of {@link rxreddit.model.Subreddit} to which user is subscribed.
     *
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

    public Observable<InfoTuple> getSubredditInfo(final @NotNull String subredditName) {
        return redditService.getSubredditInfo(subredditName)
                .concatMap((Function<Subreddit, ObservableSource<InfoTuple>>) subreddit ->
                        redditService.getSubredditRules(subredditName)
                                .map(rules -> new InfoTuple(subreddit, rules))
                );
    }
}
