package com.ddiehl.android.htn.subscriptions;

import rxreddit.model.Subreddit;

interface SubscriptionManagerView {
    void onSubredditDismissed(Subreddit subreddit);
}
