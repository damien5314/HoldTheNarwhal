package com.ddiehl.android.simpleredditreader.presenter;

import android.content.Context;
import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.android.simpleredditreader.BusProvider;
import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.events.exceptions.UserRequiredException;
import com.ddiehl.android.simpleredditreader.events.requests.HideEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadLinksEvent;
import com.ddiehl.android.simpleredditreader.events.requests.SaveEvent;
import com.ddiehl.android.simpleredditreader.events.requests.VoteEvent;
import com.ddiehl.android.simpleredditreader.events.responses.CommentsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.HideSubmittedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.LinksLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.SaveSubmittedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.UserIdentitySavedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.VoteSubmittedEvent;
import com.ddiehl.android.simpleredditreader.view.LinksView;
import com.ddiehl.reddit.Hideable;
import com.ddiehl.reddit.Savable;
import com.ddiehl.reddit.Votable;
import com.ddiehl.reddit.listings.RedditLink;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class LinksPresenterImpl implements LinksPresenter {
    private static final String TAG = LinksPresenterImpl.class.getSimpleName();

    private Context mContext;
    private Bus mBus;
    private List<RedditLink> mLinks;
    private LinksView mLinksView;

    private String mSubreddit;
    private String mSort;
    private String mTimeSpan;
    private boolean mLinksRequested = false;

    private RedditLink mLinkSelected;

    public LinksPresenterImpl(Context context, LinksView view, String subreddit) {
        mContext = context.getApplicationContext();
        mBus = BusProvider.getInstance();
        mLinksView = view;
        mLinks = new ArrayList<>();

        mSubreddit = subreddit;
        mSort = "hot";
        mTimeSpan = "all";
    }

    @Override
    public void getLinks() {
        mLinks.clear();
        mLinksView.linksUpdated();
        getMoreLinks();
    }

    @Override
    public void getMoreLinks() {
        if (mLinksRequested)
            return;

        mLinksRequested = true;
        mLinksView.showSpinner(R.string.spinner_getting_submissions);
        String id = mLinks == null || mLinks.size() == 0
                ? null : "t3_" + mLinks.get(mLinks.size() - 1).getId();
        mBus.post(new LoadLinksEvent(mSubreddit, mSort, mTimeSpan, id));
    }

    @Override
    public RedditLink getLink(int position) {
        return mLinks.get(position);
    }

    @Override
    public int getNumLinks() {
        return mLinks.size();
    }

    @Override
    public String getSubreddit() {
        return mSubreddit;
    }

    @Override
    public String getSort() {
        return mSort;
    }

    @Override
    public String getTimeSpan() {
        return mTimeSpan;
    }

    @Override
    public void updateTitle() {
        if (mSubreddit == null) {
            mLinksView.setTitle(mContext.getString(R.string.front_page_title));
        } else {
            mLinksView.setTitle(String.format(mContext.getString(R.string.link_subreddit), mSubreddit));
        }
    }

    @Override
    public void updateSubreddit(String subreddit) {
        mSubreddit = subreddit;
        mSort = "hot";
        mTimeSpan = "all";
        updateTitle();
        getLinks();
    }

    @Override
    public void updateSort(String sort) {
        if (mSort.equals(sort)) {
            return;
        }

        mSort = sort;
        getLinks();
    }

    @Override
    public void updateTimeSpan(String timespan) {
        if (mTimeSpan.equals(timespan)) {
            return;
        }

        mTimeSpan = timespan;
        getLinks();
    }

    @Subscribe
    public void onUserIdentitySaved(UserIdentitySavedEvent event) {
        getLinks();
    }

    @Subscribe
    public void onLinksLoaded(LinksLoadedEvent event) {
        mLinksView.dismissSpinner();
        if (event.isFailed()) {
            mLinksRequested = false;
            return;
        }

        if (mSubreddit != null && mSubreddit.equals("random")) {
            mSubreddit = event.getLinks().get(0).getSubreddit();
        }

        mLinks.addAll(event.getLinks());
        mLinksView.linksUpdated();
        updateTitle();
        mLinksRequested = false;
    }

    @Subscribe
    public void onCommentsLoaded(CommentsLoadedEvent event) {
        mLinksView.dismissSpinner();
        if (event.isFailed()) {
            return;
        }

        RedditLink link = event.getLink();
        mLinks.clear();
        mLinks.add(link);
        mLinksView.setTitle(link.getTitle());
    }

    @Subscribe
    public void onVoteSubmitted(VoteSubmittedEvent event) {
        Votable listing = event.getListing();
        if (!(listing instanceof RedditLink))
            return;

        if (event.isFailed()) {
            mLinksView.showToast(R.string.vote_failed);
            return;
        }

        listing.applyVote(event.getDirection());
        mLinksView.linkUpdatedAt(mLinks.indexOf(listing));
    }

    @Subscribe
    public void onLinkSaved(SaveSubmittedEvent event) {
        Savable listing = event.getListing();
        if (!(listing instanceof RedditLink))
            return;

        if (event.isFailed()) {
            mLinksView.showToast(R.string.save_failed);
            return;
        }

        listing.isSaved(event.isToSave());
        mLinksView.linkUpdatedAt(mLinks.indexOf(listing));
    }

    @Subscribe
    public void onLinkHidden(HideSubmittedEvent event) {
        Hideable listing = event.getListing();
        if (!(listing instanceof RedditLink))
            return;

        if (event.isFailed()) {
            mLinksView.showToast(R.string.hide_failed);
            return;
        }

        int pos = mLinks.indexOf(listing);
        if (event.isToHide()) {
            mLinksView.showToast(R.string.link_hidden);
            mLinks.remove(pos);
            mLinksView.linkRemovedAt(pos);
        } else {
            mLinksView.linkRemovedAt(pos);
        }
    }

    @Subscribe
    public void onUserRequiredError(UserRequiredException e) {
        mLinksView.showToast(R.string.user_required);
    }

    @Override
    public void showLinkContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, RedditLink link) {
        mLinkSelected = link;
        mLinksView.showLinkContextMenu(menu, v, menuInfo, link);
        menu.findItem(R.id.action_link_save).setVisible(!link.isSaved());
        menu.findItem(R.id.action_link_unsave).setVisible(link.isSaved());
    }

    @Override
    public void openLink(RedditLink link) {
        if (link == null)
            return;

        if (link.isSelf()) {
            mLinksView.showCommentsForLink(link);
        } else {
            mLinksView.openWebViewForLink(link);
        }
    }

    @Override
    public void showCommentsForLink() {
        RedditLink link = mLinkSelected;
        mLinksView.showCommentsForLink(link);
    }

    @Override
    public void showCommentsForLink(RedditLink link) {
        mLinksView.showCommentsForLink(link);
    }

    @Override
    public void upvote() {
        RedditLink link = mLinkSelected;
        if (link.isArchived()) {
            mLinksView.showToast(R.string.listing_archived);
        } else {
            int dir = (link.isLiked() == null || !link.isLiked()) ? 1 : 0;
            mBus.post(new VoteEvent(link, "t3", dir));
        }
    }

    @Override
    public void downvote() {
        RedditLink link = mLinkSelected;
        if (link.isArchived()) {
            mLinksView.showToast(R.string.listing_archived);
        } else {
            int dir = (link.isLiked() == null || link.isLiked()) ? -1 : 0;
            mBus.post(new VoteEvent(link, "t3", dir));
        }
    }

    @Override
    public void saveLink() {
        RedditLink link = mLinkSelected;
        mBus.post(new SaveEvent(link, null, true));
    }

    @Override
    public void unsaveLink() {
        RedditLink link = mLinkSelected;
        mBus.post(new SaveEvent(link, null, false));
    }

    @Override
    public void shareLink() {
        RedditLink link = mLinkSelected;
        mLinksView.openShareView(link);
    }

    @Override
    public void openLinkInBrowser() {
        RedditLink link = mLinkSelected;
        mLinksView.openLinkInBrowser(link);
    }

    @Override
    public void openCommentsInBrowser() {
        RedditLink link = mLinkSelected;
        mLinksView.openCommentsInBrowser(link);
    }

    @Override
    public void hideLink() {
        RedditLink link = mLinkSelected;
        mBus.post(new HideEvent(link, true));
    }

    @Override
    public void unhideLink() {
        RedditLink link = mLinkSelected;
        mBus.post(new HideEvent(link, false));
    }

    @Override
    public void reportLink() {
        RedditLink link = mLinkSelected;
        mLinksView.showToast(R.string.implementation_pending);
    }
}
