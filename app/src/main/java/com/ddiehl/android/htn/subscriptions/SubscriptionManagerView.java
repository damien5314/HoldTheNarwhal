package com.ddiehl.android.htn.subscriptions;

import android.support.annotation.NonNull;

import rxreddit.model.Subreddit;

interface SubscriptionManagerView {
    void onSubredditDismissed(@NonNull Subreddit subreddit, int position);
}
