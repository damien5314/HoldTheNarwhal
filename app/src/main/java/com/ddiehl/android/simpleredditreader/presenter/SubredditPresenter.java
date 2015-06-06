package com.ddiehl.android.simpleredditreader.presenter;

import android.content.Context;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.events.requests.LoadSubredditEvent;
import com.ddiehl.android.simpleredditreader.events.responses.HideSubmittedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.ListingsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.SaveSubmittedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.VoteSubmittedEvent;
import com.ddiehl.android.simpleredditreader.view.ListingsView;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.RedditLink;
import com.squareup.otto.Subscribe;

import java.util.List;

public class SubredditPresenter extends AbsListingsPresenter {

    public SubredditPresenter(Context context, ListingsView view, String subreddit, String sort, String timespan) {
        super(context, view, null, subreddit, null, null, sort, timespan);
        mSort = "hot";
        mTimespan = "all";
    }

    @Override
    public void refreshData() {
        if (mListingsRequested)
            return;

        mListings.clear();
        mListingsView.listingsUpdated();
        getMoreData();
    }

    @Override
    public void getMoreData() {
        if (mListingsRequested)
            return;

        mListingsRequested = true;
        mListingsView.showSpinner(R.string.spinner_getting_submissions);
        String after = mListings == null || mListings.size() == 0
                ? null : mListings.get(mListings.size() - 1).getName();
        mBus.post(new LoadSubredditEvent(mSubreddit, mSort, mTimespan, after));
    }

    @Subscribe @Override
    public void onListingsLoaded(ListingsLoadedEvent event) {
        super.onListingsLoaded(event);
        List<Listing> listings = event.getListings();
        if (mSubreddit != null) {
            if (mSubreddit.equals("random")) {
                mSubreddit = ((RedditLink) listings.get(0)).getSubreddit();
            }
            mListingsView.setTitle(String.format(mContext.getString(R.string.link_subreddit), mSubreddit));
        } else {
            mListingsView.setTitle(mContext.getString(R.string.front_page_title));
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
