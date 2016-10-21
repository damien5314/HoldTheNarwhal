package com.ddiehl.android.htn.subscriptions;

import android.support.annotation.NonNull;

import rxreddit.model.Subreddit;

interface SubscriptionManagerView {

    void onSubredditClicked(@NonNull Subreddit subreddit, int position);

    void onSubredditDismissed(@NonNull Subreddit subreddit, int position);
}
