package com.ddiehl.android.simpleredditreader.presenter;

import android.content.Context;

import com.ddiehl.android.simpleredditreader.events.requests.LoadSubredditEvent;
import com.ddiehl.android.simpleredditreader.events.responses.HideSubmittedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.ListingsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.SaveSubmittedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.VoteSubmittedEvent;
import com.ddiehl.android.simpleredditreader.view.ListingsView;
import com.ddiehl.reddit.listings.RedditLink;
import com.flurry.android.FlurryAgent;
import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.Map;

public class SubredditPresenter extends AbsListingsPresenter {

    public SubredditPresenter(Context context, ListingsView view, String subreddit,
                              String sort, String timespan) {
        super(context, view, null, null, subreddit, null, null, sort, timespan);
    }

    @Override
    public void requestData() {
        mBus.post(new LoadSubredditEvent(mSubreddit, mSort, mTimespan, mNextPageListingId));

        // Log analytics event
        Map<String, String> params = new HashMap<>();
        params.put("subreddit", mSubreddit);
        params.put("sort", mSort);
        params.put("timespan", mTimespan);
        FlurryAgent.logEvent("view subreddit", params);
    }

    @Subscribe @Override
    public void onListingsLoaded(ListingsLoadedEvent event) {
        super.onListingsLoaded(event);

        if (mSubreddit != null && mSubreddit.equals("random")) {
            mSubreddit = ((RedditLink) mListings.get(0)).getSubreddit();
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
