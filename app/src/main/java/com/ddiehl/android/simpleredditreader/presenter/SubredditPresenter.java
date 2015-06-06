package com.ddiehl.android.simpleredditreader.presenter;

import android.content.Context;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.events.requests.LoadLinksEvent;
import com.ddiehl.android.simpleredditreader.events.responses.ListingsLoadedEvent;
import com.ddiehl.android.simpleredditreader.view.ListingsView;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.RedditLink;

import java.util.List;

public class SubredditPresenter extends AbsListingsPresenter {

    public SubredditPresenter(Context context, ListingsView view, String subreddit, String sort, String timespan) {
        super(context, view, null, subreddit, null, null, sort, timespan);
        mSort = "hot";
        mTimespan = "all";
    }

    @Override
    public void refreshData() {
        getLinks();
    }

    @Override
    public void getMoreData() {
        getMoreLinks();
    }

    private void getLinks() {
        mListings.clear();
        mListingsView.listingsUpdated();
        getMoreLinks();
    }

    private void getMoreLinks() {
        mListingsView.showSpinner(R.string.spinner_getting_submissions);
        String after = mListings == null || mListings.size() == 0
                ? null : mListings.get(mListings.size() - 1).getName();
        mBus.post(new LoadLinksEvent(mSubreddit, mSort, mTimespan, after));
    }

    @Override
    public void onListingsLoaded(ListingsLoadedEvent event) {
        super.onListingsLoaded(event);
        List<Listing> listings = event.getListings();
        if (mSubreddit != null && mSubreddit.equals("random")) {
            mSubreddit = ((RedditLink) listings.get(0)).getSubreddit();
        }
    }
}
