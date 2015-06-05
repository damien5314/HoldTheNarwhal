package com.ddiehl.android.simpleredditreader.presenter;

import android.content.Context;
import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.android.simpleredditreader.BusProvider;
import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.RedditPreferences;
import com.ddiehl.android.simpleredditreader.events.exceptions.UserRequiredException;
import com.ddiehl.android.simpleredditreader.events.requests.HideEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadCommentsEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadLinksEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadMoreChildrenEvent;
import com.ddiehl.android.simpleredditreader.events.requests.SaveEvent;
import com.ddiehl.android.simpleredditreader.events.requests.VoteEvent;
import com.ddiehl.android.simpleredditreader.events.responses.CommentsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.HideSubmittedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.LinksLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.MoreChildrenLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.SaveSubmittedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.UserIdentitySavedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.VoteSubmittedEvent;
import com.ddiehl.android.simpleredditreader.view.CommentsView;
import com.ddiehl.android.simpleredditreader.view.LinksView;
import com.ddiehl.reddit.Hideable;
import com.ddiehl.reddit.Savable;
import com.ddiehl.reddit.Votable;
import com.ddiehl.reddit.listings.AbsRedditComment;
import com.ddiehl.reddit.listings.CommentBankList;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.RedditComment;
import com.ddiehl.reddit.listings.RedditLink;
import com.ddiehl.reddit.listings.RedditMoreComments;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class ListingPresenterImpl implements ListingPresenter {

    private Context mContext;
    private Bus mBus;
    private RedditPreferences mPreferences;
    private List<Listing> mListings;
//    private RedditLink mRedditLink;
//    private CommentBank mCommentBank;
    private LinksView mLinksView;
    private CommentsView mCommentsView;

    private String mUsername;
    private String mSubreddit;
    private String mArticleId;
    private String mCommentId;
    private String mSort;
    private String mTimespan;

    private Listing mListingSelected;
//    private RedditLink mLinkSelected;
//    private RedditComment mCommentSelected;

    public ListingPresenterImpl(Context context, LinksView linksView, CommentsView commentsView,
                                String username, String subreddit, String article, String comment, String sort, String timespan) {
        mContext = context.getApplicationContext();
        mBus = BusProvider.getInstance();
        mPreferences = RedditPreferences.getInstance(mContext);
        mLinksView = linksView;
        mCommentsView = commentsView;
        mUsername = username;
        mSubreddit = subreddit;
        mArticleId = article;
        mCommentId = comment;
        mSort = sort;
        mTimespan = timespan;

        mListings = new ArrayList<>();
    }

    @Override
    public void getLinks() {
        mListings.clear();
        mLinksView.linksUpdated();
        getMoreLinks();
    }

    @Override
    public void getMoreLinks() {
        mLinksView.showSpinner(R.string.spinner_getting_submissions);
        String after = mListings == null || mListings.size() == 0
                ? null : "t3_" + mListings.get(mListings.size() - 1).getId();
        mBus.post(new LoadLinksEvent(mSubreddit, mSort, mTimespan, after));
    }

    @Override
    public RedditLink getLink(int position) {
        return (RedditLink) mListings.get(position);
    }

    @Override
    public int getNumLinks() {
        return mListings.size();
    }

    @Override
    public String getSubreddit() {
        return mSubreddit;
    }

    @Override
    public String getTimespan() {
        return mTimespan;
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
        mTimespan = "all";
        updateTitle();
        getLinks();
    }

    @Override
    public void updateSort(String sort, String timespan) {
        if (mSort.equals(sort) && mTimespan.equals(timespan)) {
            return;
        }

        mSort = sort;
        mTimespan = timespan;
        getLinks();
    }

    @Subscribe
    public void onUserIdentitySaved(UserIdentitySavedEvent event) {
        // TODO Generic refresh() function in BaseView?
        getLinks();
//        getComments();
    }

    @Subscribe
    public void onLinksLoaded(LinksLoadedEvent event) {
        mLinksView.dismissSpinner();
        if (event.isFailed()) {
            return;
        }

        if (mSubreddit != null && mSubreddit.equals("random")) {
            mSubreddit = event.getLinks().get(0).getSubreddit();
        }

        mListings.addAll(event.getLinks());
        mLinksView.linksUpdated();
        updateTitle();
    }

    @Subscribe
    public void onCommentsLoaded(CommentsLoadedEvent event) {
        mLinksView.dismissSpinner();
        if (event.isFailed()) {
            return;
        }

        RedditLink link = event.getLink();
        mListings.clear();
        mListings.add(link);
        mLinksView.setTitle(link.getTitle());

        mRedditLink = event.getLink();

        List<AbsRedditComment> comments = event.getComments();
        AbsRedditComment.flattenCommentList(comments);
        mCommentBank.setData(comments);
        mCommentsView.commentsUpdated();
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

    @Override
    public void getComments() {
        mCommentsView.showSpinner(null);
        mBus.post(new LoadCommentsEvent(mSubreddit, mArticleId, mSort, mCommentId));
    }

    @Override
    public RedditLink getLink() {
        return mRedditLink;
    }

    @Override
    public void setLink(RedditLink link) {
        mRedditLink = link;
    }

    @Override
    public void showMoreChildren(RedditMoreComments comment) {
        mCommentsView.showSpinner(null);
        List<String> children = comment.getChildren();
        mBus.post(new LoadMoreChildrenEvent(mRedditLink, comment, children, mSort));
    }

    @Override
    public void updateSort() {
        String sort = mContext.getSharedPreferences(RedditPreferences.PREFS_USER, Context.MODE_PRIVATE)
                .getString(RedditPreferences.PREF_COMMENT_SORT, null);
        updateSort(sort);
    }

    @Override
    public void updateSort(String sort) {
        if (!mSort.equals(sort)) {
            mSort = sort;
            mPreferences.saveCommentSort(mSort);
            getComments();
        }
    }

    @Override
    public AbsRedditComment getCommentAtPosition(int position) {
        return mCommentBank.getVisibleComment(position);
    }

    @Override
    public void toggleThreadVisible(AbsRedditComment comment) {
        mCommentBank.toggleThreadVisible(comment);
        mCommentsView.commentsUpdated();
    }

    @Override
    public String getSort() {
        return mSort;
    }

    @Override
    public int getNumComments() {
        return mCommentBank.getNumVisible();
    }

    @Override
    public void showCommentContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, RedditComment comment) {
        mCommentSelected = comment;
        mCommentsView.showCommentContextMenu(menu, v, menuInfo, comment);

        menu.findItem(R.id.action_comment_save).setVisible(!comment.isSaved());
        menu.findItem(R.id.action_comment_unsave).setVisible(comment.isSaved());
    }

    @Override
    public void navigateToCommentThread(String commentId) {
        mCommentId = commentId.substring(3); // Remove type prefix
        getComments();
    }

    @Subscribe
    public void onMoreChildrenLoaded(MoreChildrenLoadedEvent event) {
        mCommentsView.dismissSpinner();
        if (event.isFailed()) {
            return;
        }

        RedditMoreComments parentStub = event.getParentStub();
        List<AbsRedditComment> comments = event.getComments();

        if (comments.size() == 0) {
            mCommentBank.remove(parentStub);
        } else {
            AbsRedditComment.setDepthForCommentsList(comments, parentStub.getDepth());

            for (int i = 0; i < mCommentBank.size(); i++) {
                AbsRedditComment comment = mCommentBank.get(i);
                if (comment instanceof RedditMoreComments) {
                    String id = ((RedditMoreComments) comment).getId();
                    if (id.equals(parentStub.getId())) { // Found the base comment
                        mCommentBank.remove(i);
                        mCommentBank.addAll(i, comments);
                        break;
                    }
                }
            }
        }

        mCommentsView.commentsUpdated();
    }

    @Subscribe
    public void onVoteSubmitted(VoteSubmittedEvent event) {
        Votable listing = event.getListing();
        if (!(listing instanceof RedditComment))
            return;

        if (event.isFailed()) {
            mCommentsView.showToast(R.string.vote_failed);
            return;
        }

        listing.applyVote(event.getDirection());
        mCommentsView.commentUpdatedAt(mCommentBank.visibleIndexOf(listing));
    }

    @Subscribe
    public void onCommentSaved(SaveSubmittedEvent event) {
        Savable listing = event.getListing();
        if (!(listing instanceof RedditComment))
            return;

        if (event.isFailed()) {
            mCommentsView.showToast(R.string.save_failed);
            return;
        }

        listing.isSaved(event.isToSave());
        mCommentsView.commentUpdatedAt(mCommentBank.visibleIndexOf(listing));
    }

    @Override
    public void openReplyView() {
        RedditComment comment = mCommentSelected;
        if (comment.isArchived()) {
            mCommentsView.showToast(R.string.listing_archived);
        } else {
            mCommentsView.openReplyView(comment);
        }
    }

    @Override
    public void upvote() {
        RedditComment comment = mCommentSelected;
        if (comment.isArchived()) {
            mCommentsView.showToast(R.string.listing_archived);
        } else {
            int dir = (comment.isLiked() == null || !comment.isLiked()) ? 1 : 0;
            mBus.post(new VoteEvent(comment, "t1", dir));
        }
    }

    @Override
    public void downvote() {
        RedditComment comment = mCommentSelected;
        if (comment.isArchived()) {
            mCommentsView.showToast(R.string.listing_archived);
        } else {
            int dir = (comment.isLiked() == null || comment.isLiked()) ? -1 : 0;
            mBus.post(new VoteEvent(comment, "t1", dir));
        }
    }

    @Override
    public void saveComment() {
        RedditComment comment = mCommentSelected;
        mBus.post(new SaveEvent(comment, null, true));
    }

    @Override
    public void unsaveComment() {
        RedditComment comment = mCommentSelected;
        mBus.post(new SaveEvent(comment, null, false));
    }

    @Override
    public void shareComment() {
        RedditComment comment = mCommentSelected;
        mCommentsView.openShareView(comment);
    }

    @Override
    public void openCommentInBrowser() {
        RedditComment comment = mCommentSelected;
        mCommentsView.openCommentInBrowser(comment);
    }

    @Override
    public void reportComment() {
        RedditComment comment = mCommentSelected;
        mCommentsView.showToast(R.string.implementation_pending);
    }
}
