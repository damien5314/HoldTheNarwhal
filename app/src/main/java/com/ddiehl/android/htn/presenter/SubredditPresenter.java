/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.presenter;

import android.content.Context;

import com.ddiehl.android.htn.events.requests.GetSubredditInfoEvent;
import com.ddiehl.android.htn.events.requests.LoadSubredditEvent;
import com.ddiehl.android.htn.events.responses.HideSubmittedEvent;
import com.ddiehl.android.htn.events.responses.ListingsLoadedEvent;
import com.ddiehl.android.htn.events.responses.SaveSubmittedEvent;
import com.ddiehl.android.htn.events.responses.SubredditInfoLoadedEvent;
import com.ddiehl.android.htn.events.responses.UserIdentitySavedEvent;
import com.ddiehl.android.htn.events.responses.VoteSubmittedEvent;
import com.ddiehl.android.htn.view.ListingsView;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.Link;
import com.ddiehl.reddit.listings.Subreddit;
import com.squareup.otto.Subscribe;

public class SubredditPresenter extends AbsListingsPresenter {

    private Subreddit mSubredditInfo;

    public SubredditPresenter(Context context, ListingsView view, String subreddit,
                              String sort, String timespan) {
        super(context, view, null, null, subreddit, sort, timespan);
    }

    @Override
    public void requestData() {
        if (mSubreddit == null || mSubreddit.equals("all") || mSubredditInfo != null) {
            mBus.post(new LoadSubredditEvent(mSubreddit, mSort, mTimespan, mNextPageListingId));
        } else {
            mBus.post(new GetSubredditInfoEvent(mSubreddit));
        }
    }

    @Subscribe @SuppressWarnings("unused")
    public void onSubredditInfoLoaded(SubredditInfoLoadedEvent event) {
        if (event.isFailed())
            return;

        mSubredditInfo = event.getSubreddit();
        UserIdentity user = getAuthorizedUser();
        if (mSubredditInfo.isOver18() && (user == null || !user.isOver18())) {
            mListingsView.displayOver18Required();
        } else {
            requestData();
        }

        mListingsView.onSubredditInfoLoaded(mSubredditInfo);
    }

    @Subscribe @Override
    public void onUserIdentitySaved(UserIdentitySavedEvent event) {
        super.onUserIdentitySaved(event);
    }

    @Subscribe @Override
    public void onListingsLoaded(ListingsLoadedEvent event) {
        super.onListingsLoaded(event);

        if (mSubreddit != null && mSubreddit.equals("random")) {
            mSubreddit = ((Link) mListings.get(0)).getSubreddit();
        }
    }

    @Subscribe @Override
    public void onVoteSubmitted(VoteSubmittedEvent event) {
        super.onVoteSubmitted(event);
    }

    @Subscribe @Override
    public void onListingSaved(SaveSubmittedEvent event) {
        super.onListingSaved(event);
    }

    @Subscribe @Override
    public void onListingHidden(HideSubmittedEvent event) {
        super.onListingHidden(event);
    }
}
