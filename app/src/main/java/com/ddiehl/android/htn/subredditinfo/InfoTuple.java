package com.ddiehl.android.htn.subredditinfo;

import rxreddit.model.Subreddit;
import rxreddit.model.SubredditRules;

public class InfoTuple {
    public Subreddit subreddit;
    public SubredditRules rules;

    public InfoTuple(Subreddit subreddit, SubredditRules rules) {
        this.subreddit = subreddit;
        this.rules = rules;
    }
}
