/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.presenter;

import android.content.Context;
import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.android.htn.AccessTokenManager;
import com.ddiehl.android.htn.BusProvider;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.SettingsManager;
import com.ddiehl.android.htn.events.requests.HideEvent;
import com.ddiehl.android.htn.events.requests.SaveEvent;
import com.ddiehl.android.htn.events.requests.VoteEvent;
import com.ddiehl.android.htn.events.responses.HideSubmittedEvent;
import com.ddiehl.android.htn.events.responses.ListingsLoadedEvent;
import com.ddiehl.android.htn.events.responses.SaveSubmittedEvent;
import com.ddiehl.android.htn.events.responses.UserIdentitySavedEvent;
import com.ddiehl.android.htn.events.responses.UserProfileSummaryLoadedEvent;
import com.ddiehl.android.htn.events.responses.VoteSubmittedEvent;
import com.ddiehl.android.htn.view.ListingsView;
import com.ddiehl.reddit.Archivable;
import com.ddiehl.reddit.Hideable;
import com.ddiehl.reddit.Savable;
import com.ddiehl.reddit.Votable;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.CommentStub;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.Link;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

public abstract class AbsListingsPresenter implements ListingsPresenter {

    Context mContext;
    Bus mBus;
    AccessTokenManager mAccessTokenManager;
    IdentityManager mIdentityManager;
    SettingsManager mSettingsManager;

    List<Listing> mListings;
    ListingsView mListingsView;

    String mShow;
    String mUsernameContext;
    String mSubreddit;
    String mSort;
    String mTimespan;

    Listing mListingSelected;
    boolean mListingsRequested = false;
    String mNextPageListingId;

    public AbsListingsPresenter(Context context, ListingsView view,
                                String show, String username, String subreddit, String sort, String timespan) {
        mContext = context.getApplicationContext();
        mBus = BusProvider.getInstance();
        mAccessTokenManager = AccessTokenManager.getInstance(context);
        mIdentityManager = IdentityManager.getInstance(mContext);
        mSettingsManager = SettingsManager.getInstance(mContext);
        mListingsView = view;
        mShow = show;
        mUsernameContext = username;
        mSubreddit = subreddit;
        mSort = sort;
        mTimespan = timespan;

        mListings = new ArrayList<>();
    }

    @Override
    public void refreshData() {
        if (mListingsRequested)
            return;

        mListings.clear();
        mListingsView.listingsUpdated();
        mNextPageListingId = null;
        getMoreData();
    }

    @Override
    public void getMoreData() {
        if (mListingsRequested)
            return;

        mListingsRequested = true;
        mListingsView.showSpinner(null);
        requestData();
    }

    abstract void requestData();

    @Override
    public void setData(List<Listing> data) {
        mListings.clear();
        mListings.addAll(data);
    }

    @Override
    public int getNumListings() {
        return mListings.size();
    }

    @Override
    public Listing getListing(int position) {
        return mListings.get(position);
    }

    @Override
    public String getShow() {
        return mShow;
    }

    @Override
    public String getUsernameContext() {
        return mUsernameContext;
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
    public String getTimespan() {
        return mTimespan;
    }

    @Override
    public String getNextPageListingId() {
        return mNextPageListingId;
    }

    @Override
    public void updateSubreddit(String subreddit) {
        mSubreddit = subreddit;
        mSort = "hot";
        mTimespan = "all";
        refreshData();
    }

    @Override
    public void updateSort() {
        String sort = "hot";
        String timespan = "all";
        updateSort(sort, timespan);
    }

    @Override
    public void updateSort(String sort) {
        if (!mSort.equals(sort)) {
            mSort = sort;
            mSettingsManager.saveCommentSort(mSort);
            refreshData();
        }
    }

    @Override
    public void updateSort(String sort, String timespan) {
        if (mSort.equals(sort) && mTimespan.equals(timespan)) {
            return;
        }

        mSort = sort;
        mTimespan = timespan;
        refreshData();
    }

    @Subscribe
    public void onUserIdentitySaved(UserIdentitySavedEvent event) {
        refreshData();
    }

    @Subscribe
    public void onListingsLoaded(ListingsLoadedEvent event) {
        mListingsView.dismissSpinner();
        if (event.isFailed()) {
            mListingsRequested = false;
            return;
        }

        List<Listing> listings = event.getListings();
        mListings.addAll(listings);
        mListingsView.listingsUpdated();
        mNextPageListingId = event.getResponse().getData().getAfter();
        mListingsRequested = false;
    }

    @Subscribe
    public void onUserProfileSummaryLoaded(UserProfileSummaryLoadedEvent event) {
        mListingsView.dismissSpinner();
        if (event.isFailed()) {
            mListingsRequested = false;
            return;
        }

        // TODO Display data on success
    }

    @Subscribe
    public void onVoteSubmitted(VoteSubmittedEvent event) {
        Votable listing = event.getListing();

        if (event.isFailed()) {
            mListingsView.showToast(R.string.vote_failed);
            return;
        }

        listing.applyVote(event.getDirection());
        mListingsView.listingUpdatedAt(mListings.indexOf(listing));
    }

    @Subscribe
    public void onListingSaved(SaveSubmittedEvent event) {
        Savable listing = event.getListing();

        if (event.isFailed()) {
            mListingsView.showToast(R.string.save_failed);
            return;
        }

        listing.isSaved(event.isToSave());
        mListingsView.listingUpdatedAt(mListings.indexOf(listing));
    }

    @Subscribe
    public void onListingHidden(HideSubmittedEvent event) {
        Hideable listing = event.getListing();

        if (event.isFailed()) {
            mListingsView.showToast(R.string.hide_failed);
            return;
        }

        int pos = mListings.indexOf(listing);
        if (event.isToHide()) {
            mListingsView.showToast(R.string.link_hidden);
            mListings.remove(pos);
            mListingsView.listingRemovedAt(pos);
        } else {
            mListingsView.listingRemovedAt(pos);
        }
    }

    @Override
    public void showLinkContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo,
                                    Link link) {
        mListingSelected = link;
        mListingsView.showLinkContextMenu(menu, v, link);
        menu.findItem(R.id.action_link_save).setVisible(!link.isSaved());
        menu.findItem(R.id.action_link_unsave).setVisible(link.isSaved());
    }

    @Override
    public void openLink(Link link) {
        if (link == null)
            return;

        if (link.isSelf()) {
            mListingsView.showCommentsForLink(link.getSubreddit(), link.getId(), null);
        } else {
            mListingsView.openLinkInWebView(link);
        }
    }

    @Override
    public void showCommentsForLink() {
        Link link = (Link) mListingSelected;
        mListingsView.showCommentsForLink(link.getSubreddit(), link.getId(), null);
    }

    @Override
    public void showCommentsForLink(Link link) {
        mListingsView.showCommentsForLink(link.getSubreddit(), link.getId(), null);
    }

    @Override
    public void upvoteLink() {
        Votable votable = (Votable) mListingSelected;
        int dir = (votable.isLiked() == null || !votable.isLiked()) ? 1 : 0;
        vote(dir);
    }

    @Override
    public void downvoteLink() {
        Votable votable = (Votable) mListingSelected;
        int dir = (votable.isLiked() == null || votable.isLiked()) ? -1 : 0;
        vote(dir);
    }

    @Override
    public void saveLink() {
        if (!mAccessTokenManager.isUserAuthorized()) {
            mListingsView.showToast(R.string.user_required);
            return;
        }

        Link link = (Link) mListingSelected;
        mBus.post(new SaveEvent(link, null, true));
    }

    @Override
    public void unsaveLink() {
        if (!mAccessTokenManager.isUserAuthorized()) {
            mListingsView.showToast(R.string.user_required);
            return;
        }

        Link link = (Link) mListingSelected;
        mBus.post(new SaveEvent(link, null, false));
    }

    @Override
    public void shareLink() {
        Link link = (Link) mListingSelected;
        mListingsView.openShareView(link);
    }

    @Override
    public void openLinkUserProfile() {
        Link link = (Link) mListingSelected;
        mListingsView.openUserProfileView(link);
    }

    @Override
    public void openLinkUserProfile(Link link) {
//        String username = link.getAuthor();
        mListingsView.openUserProfileView(link);
    }

    @Override
    public void openLinkInBrowser() {
        Link link = (Link) mListingSelected;
        mListingsView.openLinkInBrowser(link);
    }

    @Override
    public void openCommentsInBrowser() {
        Link link = (Link) mListingSelected;
        mListingsView.openCommentsInBrowser(link);
    }

    @Override
    public void hideLink() {
        if (!mAccessTokenManager.isUserAuthorized()) {
            mListingsView.showToast(R.string.user_required);
            return;
        }

        Link link = (Link) mListingSelected;
        mBus.post(new HideEvent(link, true));
    }

    @Override
    public void unhideLink() {
        if (!mAccessTokenManager.isUserAuthorized()) {
            mListingsView.showToast(R.string.user_required);
            return;
        }

        Link link = (Link) mListingSelected;
        mBus.post(new HideEvent(link, false));
    }

    @Override
    public void reportLink() {
        if (!mAccessTokenManager.isUserAuthorized()) {
            mListingsView.showToast(R.string.user_required);
            return;
        }

        Link link = (Link) mListingSelected;
        mListingsView.showToast(R.string.implementation_pending);
    }

    @Override
    public void showCommentContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo,
                                       Comment comment) {
        mListingSelected = comment;
        mListingsView.showCommentContextMenu(menu, v, comment);

        menu.findItem(R.id.action_comment_save).setVisible(!comment.isSaved());
        menu.findItem(R.id.action_comment_unsave).setVisible(comment.isSaved());
    }

    @Override
    public void showCommentThread(String subreddit, String linkId, String commentId) {
        linkId = linkId.substring(3); // Trim type prefix
        mListingsView.showCommentThread(subreddit, linkId, commentId);
    }

    @Override
    public void getMoreChildren(CommentStub comment) {
        // Comment stubs cannot appear in a listing view
    }

    @Override
    public void openCommentPermalink() {
        Comment comment = (Comment) mListingSelected;
        showCommentThread(comment.getSubreddit(), comment.getLinkId(), comment.getId());
    }

    @Override
    public void openReplyView() {
        Comment comment = (Comment) mListingSelected;
        if (comment.isArchived()) {
            mListingsView.showToast(R.string.listing_archived);
        } else {
            mListingsView.openReplyView(comment);
        }
    }

    @Override
    public void upvoteComment() {
        Votable votable = (Votable) mListingSelected;
        int dir = (votable.isLiked() == null || !votable.isLiked()) ? 1 : 0;
        vote(dir);
    }

    @Override
    public void downvoteComment() {
        Votable votable = (Votable) mListingSelected;
        int dir = (votable.isLiked() == null || votable.isLiked()) ? -1 : 0;
        vote(dir);
    }

    @Override
    public void saveComment() {
        if (!mAccessTokenManager.isUserAuthorized()) {
            mListingsView.showToast(R.string.user_required);
            return;
        }

        Comment comment = (Comment) mListingSelected;
        mBus.post(new SaveEvent(comment, null, true));
    }

    @Override
    public void unsaveComment() {
        if (!mAccessTokenManager.isUserAuthorized()) {
            mListingsView.showToast(R.string.user_required);
            return;
        }

        Comment comment = (Comment) mListingSelected;
        mBus.post(new SaveEvent(comment, null, false));
    }

    @Override
    public void shareComment() {
        Comment comment = (Comment) mListingSelected;
        mListingsView.openShareView(comment);
    }

    @Override
    public void openCommentUserProfile() {
        Comment comment = (Comment) mListingSelected;
        mListingsView.openUserProfileView(comment);
    }

    @Override
    public void openCommentUserProfile(Comment comment) {
        mListingsView.openUserProfileView(comment);
    }

    @Override
    public void openCommentInBrowser() {
        Comment comment = (Comment) mListingSelected;
        mListingsView.openCommentInBrowser(comment);
    }

    @Override
    public void reportComment() {
        if (!mAccessTokenManager.isUserAuthorized()) {
            mListingsView.showToast(R.string.user_required);
            return;
        }

        Comment comment = (Comment) mListingSelected;
        mListingsView.showToast(R.string.implementation_pending);
    }

    @Override
    public void openCommentLink(Comment comment) {
//        Comment comment = (Comment) mListingSelected;
        mListingsView.showCommentsForLink(comment.getSubreddit(), comment.getLinkId().substring(3), null);
    }

    @Override
    public UserIdentity getAuthorizedUser() {
        return mIdentityManager.getUserIdentity();
    }

    private void vote(int dir) {
        Listing listing = mListingSelected;
        if (((Archivable) listing).isArchived()) {
            mListingsView.showToast(R.string.listing_archived);
        } else if (!mAccessTokenManager.isUserAuthorized()) {
            mListingsView.showToast(R.string.user_required);
        } else {
            Votable votable = (Votable) listing;
            mBus.post(new VoteEvent(votable, listing.getKind(), dir));
        }
    }
}
