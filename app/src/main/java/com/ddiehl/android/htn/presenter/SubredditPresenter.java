package com.ddiehl.android.htn.presenter;

import com.ddiehl.android.htn.events.responses.HideSubmittedEvent;
import com.ddiehl.android.htn.events.responses.SaveSubmittedEvent;
import com.ddiehl.android.htn.events.responses.VoteSubmittedEvent;
import com.ddiehl.android.htn.view.ListingsView;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.Link;
import com.ddiehl.reddit.listings.ListingResponse;
import com.ddiehl.reddit.listings.Subreddit;
import com.squareup.otto.Subscribe;

import rx.functions.Action1;

public class SubredditPresenter extends AbsListingsPresenter {
    private Subreddit mSubredditInfo;

    public SubredditPresenter(
            MainView main, ListingsView view, String subreddit, String sort, String timespan) {
        super(main, view, null, null, subreddit, sort, timespan);
    }

    @Override
    public void requestData() {
        if (mSubreddit == null || mSubreddit.equals("all") || mSubredditInfo != null) {
            mAnalytics.logLoadSubreddit(mSubreddit, mSort, mTimespan);
            mRedditService.loadLinks(mSubreddit, mSort, mTimespan, mNextPageListingId)
                    .subscribe(onListingsLoaded());
        } else {
            mListingsRequested = false;
            mRedditService.getSubredditInfo(mSubreddit)
                    .subscribe(onSubredditInfoLoaded());
        }
    }

    private Action1<Subreddit> onSubredditInfoLoaded() {
        return subreddit -> {
            mSubredditInfo = subreddit;
            UserIdentity user = getAuthorizedUser();
            if ((mSubredditInfo != null && mSubredditInfo.isOver18())
                    && (user == null || !user.isOver18())) {
                mMainView.showNsfwWarningDialog();
            } else {
                requestData();
            }
            mMainView.loadImageIntoDrawerHeader(subreddit.getHeaderImageUrl());
        };
    }

    protected Action1<ListingResponse> onListingsLoaded() {
        return (response) -> {
            super.onListingsLoaded().call(response);
            if (mSubreddit != null && mSubreddit.equals("random")) {
                mSubreddit = ((Link) mListings.get(0)).getSubreddit();
            }
        };
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
