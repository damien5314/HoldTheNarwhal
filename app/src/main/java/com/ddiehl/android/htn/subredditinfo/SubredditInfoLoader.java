package com.ddiehl.android.htn.subredditinfo;

import android.support.annotation.NonNull;

import com.ddiehl.android.htn.HoldTheNarwhal;

import javax.inject.Inject;

import rx.Observable;
import rxreddit.api.RedditService;

public class SubredditInfoLoader {

    @Inject RedditService mRedditService;

    public SubredditInfoLoader() {
        HoldTheNarwhal.getApplicationComponent().inject(this);
    }

    Observable<InfoTuple> getSubredditInfo(final @NonNull String subreddit) {
        return Observable.combineLatest(
                mRedditService.getSubredditInfo(subreddit),
                mRedditService.getSubredditRules(subreddit),
//                mRedditService.getSubredditSidebar(subreddit),
                (subreddit2, rules) -> {
                    InfoTuple tuple = new InfoTuple();
                    tuple.subreddit = subreddit2;
                    tuple.rules = rules;
                    return tuple;
                }
        );
    }
}
