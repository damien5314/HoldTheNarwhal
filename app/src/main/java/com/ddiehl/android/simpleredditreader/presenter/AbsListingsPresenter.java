package com.ddiehl.android.simpleredditreader.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.android.simpleredditreader.BusProvider;
import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.RedditIdentityManager;
import com.ddiehl.android.simpleredditreader.RedditPreferences;
import com.ddiehl.android.simpleredditreader.events.exceptions.UserRequiredException;
import com.ddiehl.android.simpleredditreader.events.requests.HideEvent;
import com.ddiehl.android.simpleredditreader.events.requests.SaveEvent;
import com.ddiehl.android.simpleredditreader.events.requests.VoteEvent;
import com.ddiehl.android.simpleredditreader.events.responses.HideSubmittedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.ListingsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.SaveSubmittedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.UserIdentitySavedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.VoteSubmittedEvent;
import com.ddiehl.android.simpleredditreader.view.ListingsView;
import com.ddiehl.reddit.Archivable;
import com.ddiehl.reddit.Hideable;
import com.ddiehl.reddit.Savable;
import com.ddiehl.reddit.Votable;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.RedditComment;
import com.ddiehl.reddit.listings.RedditLink;
import com.ddiehl.reddit.listings.RedditMoreComments;
import com.flurry.android.FlurryAgent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbsListingsPresenter implements ListingsPresenter {

    protected Context mContext;
    protected Bus mBus;
    protected RedditPreferences mPreferences;
    protected List<Listing> mListings;
    protected ListingsView mListingsView;

    private RedditIdentityManager mIdentityManager;

    protected String mShow;
    protected String mUsernameContext;
    protected String mSubreddit;
    protected String mSort;
    protected String mTimespan;

    protected Listing mListingSelected;
    protected boolean mListingsRequested = false;
    protected String mNextPageListingId;

    public AbsListingsPresenter(Context context, ListingsView view,
                                String show, String username, String subreddit, String article,
                                String comment, String sort, String timespan) {
        mContext = context.getApplicationContext();
        mBus = BusProvider.getInstance();
        mPreferences = RedditPreferences.getInstance(mContext);
        mListingsView = view;
        mIdentityManager = RedditIdentityManager.getInstance(context);
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
            mPreferences.saveCommentSort(mSort);
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

    @Subscribe
    public void onUserRequiredError(UserRequiredException e) {
        mListingsView.showToast(R.string.user_required);
    }

    @Override
    public void showLinkContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo,
                                    RedditLink link) {
        mListingSelected = link;
        mListingsView.showLinkContextMenu(menu, v, menuInfo, link);
        menu.findItem(R.id.action_link_save).setVisible(!link.isSaved());
        menu.findItem(R.id.action_link_unsave).setVisible(link.isSaved());
    }

    @Override
    public void openLink(RedditLink link) {
        if (link == null)
            return;

        if (link.isSelf()) {
            mListingsView.showCommentsForLink(link.getSubreddit(), link.getId());
        } else {
            mListingsView.openWebViewForLink(link);
        }
    }

    @Override
    public void showCommentsForLink() {
        RedditLink link = (RedditLink) mListingSelected;
        mListingsView.showCommentsForLink(link.getSubreddit(), link.getId());
    }

    @Override
    public void showCommentsForLink(RedditLink link) {
        mListingsView.showCommentsForLink(link.getSubreddit(), link.getId());
    }

    @Override
    public void upvoteLink() {
        Listing listing = mListingSelected;
        if (((Archivable) listing).isArchived()) {
            mListingsView.showToast(R.string.listing_archived);
        } else {
            Votable votable = (Votable) listing;
            int dir = (votable.isLiked() == null || !votable.isLiked()) ? 1 : 0;
            mBus.post(new VoteEvent(votable, listing.getKind(), dir));
        }
    }

    @Override
    public void downvoteLink() {
        Listing listing = mListingSelected;
        if (((Archivable) listing).isArchived()) {
            mListingsView.showToast(R.string.listing_archived);
        } else {
            Votable votable = (Votable) listing;
            int dir = (votable.isLiked() == null || votable.isLiked()) ? -1 : 0;
            mBus.post(new VoteEvent(votable, listing.getKind(), dir));
        }
    }

    @Override
    public void saveLink() {
        RedditLink link = (RedditLink) mListingSelected;
        mBus.post(new SaveEvent(link, null, true));

        // Log analytics event
        Map<String, String> params = new HashMap<>();
        params.put("type", "link");
        params.put("subreddit", link.getSubreddit());
        FlurryAgent.logEvent("save", params);
    }

    @Override
    public void unsaveLink() {
        RedditLink link = (RedditLink) mListingSelected;
        mBus.post(new SaveEvent(link, null, false));
    }

    @Override
    public void shareLink() {
        RedditLink link = (RedditLink) mListingSelected;
        mListingsView.openShareView(link);
    }

    @Override
    public void openLinkUserProfile() {
        RedditLink link = (RedditLink) mListingSelected;
        mListingsView.openUserProfileView(link);
    }

    @Override
    public void openLinkUserProfile(RedditLink link) {
//        String username = link.getAuthor();
        mListingsView.openUserProfileView(link);
    }

    @Override
    public void openLinkInBrowser() {
        RedditLink link = (RedditLink) mListingSelected;
        mListingsView.openLinkInBrowser(link);
    }

    @Override
    public void openCommentsInBrowser() {
        RedditLink link = (RedditLink) mListingSelected;
        mListingsView.openCommentsInBrowser(link);
    }

    @Override
    public void hideLink() {
        RedditLink link = (RedditLink) mListingSelected;
        mBus.post(new HideEvent(link, true));
    }

    @Override
    public void unhideLink() {
        RedditLink link = (RedditLink) mListingSelected;
        mBus.post(new HideEvent(link, false));
    }

    @Override
    public void reportLink() {
        RedditLink link = (RedditLink) mListingSelected;
        mListingsView.showToast(R.string.implementation_pending);
    }

    @Override
    public void showCommentContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo,
                                       RedditComment comment) {
        mListingSelected = comment;
        mListingsView.showCommentContextMenu(menu, v, menuInfo, comment);

        menu.findItem(R.id.action_comment_save).setVisible(!comment.isSaved());
        menu.findItem(R.id.action_comment_unsave).setVisible(comment.isSaved());
    }

    @Override
    public void navigateToCommentThread(String commentId) {
        mListingsView.navigateToCommentThread(commentId);
    }

    @Override
    public void getMoreChildren(RedditMoreComments comment) {
        // Comment stubs cannot appear in a listing view
    }

    @Override
    public void openReplyView() {
        RedditComment comment = (RedditComment) mListingSelected;
        if (comment.isArchived()) {
            mListingsView.showToast(R.string.listing_archived);
        } else {
            mListingsView.openReplyView(comment);
        }
    }

    @Override
    public void upvoteComment() {
        RedditComment comment = (RedditComment) mListingSelected;
        if (comment.isArchived()) {
            mListingsView.showToast(R.string.listing_archived);
        } else {
            int dir = (comment.isLiked() == null || !comment.isLiked()) ? 1 : 0;
            mBus.post(new VoteEvent(comment, "t1", dir));
        }
    }

    @Override
    public void downvoteComment() {
        RedditComment comment = (RedditComment) mListingSelected;
        if (comment.isArchived()) {
            mListingsView.showToast(R.string.listing_archived);
        } else {
            int dir = (comment.isLiked() == null || comment.isLiked()) ? -1 : 0;
            mBus.post(new VoteEvent(comment, "t1", dir));
        }
    }

    @Override
    public void saveComment() {
        RedditComment comment = (RedditComment) mListingSelected;
        mBus.post(new SaveEvent(comment, null, true));
    }

    @Override
    public void unsaveComment() {
        RedditComment comment = (RedditComment) mListingSelected;
        mBus.post(new SaveEvent(comment, null, false));
    }

    @Override
    public void shareComment() {
        RedditComment comment = (RedditComment) mListingSelected;
        mListingsView.openShareView(comment);
    }

    @Override
    public void openCommentUserProfile() {
        RedditComment comment = (RedditComment) mListingSelected;
        mListingsView.openUserProfileView(comment);
    }

    @Override
    public void openCommentUserProfile(RedditComment comment) {
        mListingsView.openUserProfileView(comment);
    }

    @Override
    public void openCommentInBrowser() {
        RedditComment comment = (RedditComment) mListingSelected;
        mListingsView.openCommentInBrowser(comment);
    }

    @Override
    public void reportComment() {
        RedditComment comment = (RedditComment) mListingSelected;
        mListingsView.showToast(R.string.implementation_pending);
    }

    @Override
    public void openCommentLink(RedditComment comment) {
//        RedditComment comment = (RedditComment) mListingSelected;
        mListingsView.showCommentsForLink(comment.getSubreddit(), comment.getLinkId().substring(3));
    }

    @Override
    public UserIdentity getAuthorizedUser() {
        return mIdentityManager.getUserIdentity();
    }

    @Override
    public boolean getAdsEnabled() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sp.getBoolean("pref_enable_ads", false);
    }
}
