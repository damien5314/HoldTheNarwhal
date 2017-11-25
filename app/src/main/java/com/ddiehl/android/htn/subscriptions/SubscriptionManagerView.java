package com.ddiehl.android.htn.subscriptions;

import org.jetbrains.annotations.NotNull;

import rxreddit.model.Subreddit;

interface SubscriptionManagerView {

    void onSubredditClicked(@NotNull Subreddit subreddit, int position);

    void onSubredditDismissed(@NotNull Subreddit subreddit, int position);
}
