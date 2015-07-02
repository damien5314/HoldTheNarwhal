/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.presenter;

import android.content.Context;
import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.android.htn.BusProvider;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.RedditPrefs;
import com.ddiehl.android.htn.events.requests.HideEvent;
import com.ddiehl.android.htn.events.requests.LoadLinkCommentsEvent;
import com.ddiehl.android.htn.events.requests.LoadMoreChildrenEvent;
import com.ddiehl.android.htn.events.requests.SaveEvent;
import com.ddiehl.android.htn.events.requests.VoteEvent;
import com.ddiehl.android.htn.events.responses.LinkCommentsLoadedEvent;
import com.ddiehl.android.htn.events.responses.MoreChildrenLoadedEvent;
import com.ddiehl.android.htn.events.responses.SaveSubmittedEvent;
import com.ddiehl.android.htn.events.responses.UserIdentitySavedEvent;
import com.ddiehl.android.htn.events.responses.VoteSubmittedEvent;
import com.ddiehl.android.htn.view.LinkCommentsView;
import com.ddiehl.reddit.Archivable;
import com.ddiehl.reddit.Savable;
import com.ddiehl.reddit.Votable;
import com.ddiehl.reddit.listings.AbsComment;
import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.CommentBank;
import com.ddiehl.reddit.listings.CommentBankList;
import com.ddiehl.reddit.listings.CommentStub;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.Link;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

public class LinkCommentsPresenterImpl implements LinkCommentsPresenter {

    private static final int MAX_CHILDREN_PER_REQUEST = 20;

    private Context mContext;

    private LinkCommentsView mLinkCommentsView;
    private Link mLinkContext;
    private CommentBank mCommentBank;
    private Bus mBus;
    private RedditPrefs mPreferences;

    private IdentityManager mIdentityManager;

    private String mSubreddit;
    private String mArticleId;
    private String mCommentId;
    private String mSort; // Remove this and read from preferences when needed

    private Listing mListingSelected;

    public LinkCommentsPresenterImpl(Context context, LinkCommentsView view,
                                     String subreddit, String articleId, String commentId) {
        mContext = context.getApplicationContext();
        mLinkCommentsView = view;
        mCommentBank = new CommentBankList();
        mBus = BusProvider.getInstance();
        mIdentityManager = IdentityManager.getInstance(context);
        mPreferences = RedditPrefs.getInstance(mContext);
        mSubreddit = subreddit;
        mArticleId = articleId;
        mCommentId = commentId;
        mSort = mPreferences.getCommentSort();
    }

    @Override
    public void getComments() {
        mLinkCommentsView.showSpinner(null);
        mBus.post(new LoadLinkCommentsEvent(mSubreddit, mArticleId, mSort, mCommentId));
    }

    @Subscribe
    public void onCommentsLoaded(LinkCommentsLoadedEvent event) {
        mLinkCommentsView.dismissSpinner();
        if (event.isFailed()) {
            return;
        }

        mLinkContext = event.getLink();
        mLinkCommentsView.setTitle(mLinkContext.getTitle());
        List<Listing> comments = event.getComments();
        AbsComment.Utils.flattenCommentList(comments);
        mCommentBank.clear();
        mCommentBank.addAll(comments);
        mLinkCommentsView.commentsUpdated();
    }

    @Override
    public void getMoreChildren(CommentStub comment) {
        mLinkCommentsView.showSpinner(null);
        List<String> children = comment.getChildren();
        // Truncate list of children to 20
        children = children.subList(0, Math.min(MAX_CHILDREN_PER_REQUEST, children.size()));
        mBus.post(new LoadMoreChildrenEvent(mLinkContext, comment, children, mSort));
    }

    @Subscribe
    public void onMoreChildrenLoaded(MoreChildrenLoadedEvent event) {
        mLinkCommentsView.dismissSpinner();
        if (event.isFailed()) {
            return;
        }

        CommentStub parentStub = event.getParentStub();
        List<Listing> comments = event.getComments();

        if (comments == null || comments.size() == 0) {
            mCommentBank.remove(parentStub);
        } else {
            AbsComment.Utils.setDepthForCommentsList(comments, parentStub.getDepth());

            // TODO: Pass CommentStub along with event so we don't have to search for it
            for (int i = 0; i < mCommentBank.size(); i++) {
                AbsComment comment = mCommentBank.get(i);
                if (comment instanceof CommentStub) {
                    String id = comment.getId();
                    if (id.equals(parentStub.getId())) { // Found the base comment
                        ((CommentStub) comment).removeChildren(comments);
                        ((CommentStub) comment).setCount(((CommentStub) comment).getChildren().size());
                        if (((CommentStub) comment).getCount() == 0)
                            mCommentBank.remove(i);
                        mCommentBank.addAll(i, comments);
                        break;
                    }
                }
            }
        }

        mLinkCommentsView.commentsUpdated();
    }

    @Override
    public Link getLinkContext() {
        return mLinkContext;
    }

    @Override
    public void updateSort() {
        String sort = mPreferences.getCommentSort();
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
    public AbsComment getComment(int position) {
        return mCommentBank.getVisibleComment(position);
    }

    @Override
    public void toggleThreadVisible(AbsComment comment) {
        mCommentBank.toggleThreadVisible(comment);
        mLinkCommentsView.commentsUpdated();
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
    public void showLinkContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Link link) {
        mListingSelected = link;
        mLinkCommentsView.showLinkContextMenu(menu, v, link);
        menu.findItem(R.id.action_link_save).setVisible(!link.isSaved());
        menu.findItem(R.id.action_link_unsave).setVisible(link.isSaved());
    }

    @Override
    public void openLink(Link link) {
        if (link == null)
            return;

        if (link.isSelf()) {
            mLinkCommentsView.showCommentsForLink(link.getSubreddit(), link.getId(), null);
        } else {
            mLinkCommentsView.openLinkInWebView(link);
        }
    }

    @Override
    public void showCommentsForLink() {
        mLinkCommentsView.showCommentsForLink(mLinkContext.getSubreddit(), mLinkContext.getId(), null);
    }

    @Override
    public void showCommentsForLink(Link link) {
        mLinkCommentsView.showCommentsForLink(link.getSubreddit(), link.getId(), null);
    }

    @Override
    public void upvoteLink() {
        int dir = (mLinkContext.isLiked() == null || !mLinkContext.isLiked()) ? 1 : 0;
        vote(dir);
    }

    @Override
    public void downvoteLink() {
        int dir = (mLinkContext.isLiked() == null || mLinkContext.isLiked()) ? -1 : 0;
        vote(dir);
    }

    @Override
    public void saveLink() {
        if (!mIdentityManager.isUserAuthorized()) {
            mLinkCommentsView.showToast(R.string.user_required);
            return;
        }

        mBus.post(new SaveEvent(mLinkContext, null, true));
    }

    @Override
    public void unsaveLink() {
        if (!mIdentityManager.isUserAuthorized()) {
            mLinkCommentsView.showToast(R.string.user_required);
            return;
        }

        mBus.post(new SaveEvent(mLinkContext, null, false));
    }

    @Override
    public void shareLink() {
        mLinkCommentsView.openShareView(mLinkContext);
    }

    @Override
    public void openLinkUserProfile() {
        mLinkCommentsView.openUserProfileView(mLinkContext);
    }

    @Override
    public void openLinkUserProfile(Link link) {
        mLinkCommentsView.openUserProfileView(link);
    }

    @Override
    public void openLinkInBrowser() {
        mLinkCommentsView.openLinkInBrowser(mLinkContext);
    }

    @Override
    public void openCommentsInBrowser() {
        mLinkCommentsView.openCommentsInBrowser(mLinkContext);
    }

    @Override
    public void hideLink() {
        if (!mIdentityManager.isUserAuthorized()) {
            mLinkCommentsView.showToast(R.string.user_required);
            return;
        }

        mBus.post(new HideEvent(mLinkContext, true));
    }

    @Override
    public void unhideLink() {
        if (!mIdentityManager.isUserAuthorized()) {
            mLinkCommentsView.showToast(R.string.user_required);
            return;
        }

        mBus.post(new HideEvent(mLinkContext, false));
    }

    @Override
    public void reportLink() {
        if (!mIdentityManager.isUserAuthorized()) {
            mLinkCommentsView.showToast(R.string.user_required);
            return;
        }

        mLinkCommentsView.showToast(R.string.implementation_pending);
    }

    @Override
    public void showCommentContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Comment comment) {
        mListingSelected = comment;
        mLinkCommentsView.showCommentContextMenu(menu, v, comment);
        menu.findItem(R.id.action_comment_save).setVisible(!comment.isSaved());
        menu.findItem(R.id.action_comment_unsave).setVisible(comment.isSaved());
    }

    @Override
    public void showCommentThread(String subreddit, String linkId, String commentId) {
        // Calls from a ThreadStubViewHolder will not have subreddit or linkId
        // so only set if it's not null
        mSubreddit = subreddit == null ? mSubreddit : subreddit;
        mCommentId = commentId.contains("_") ? commentId.substring(3) : commentId; // Remove type prefix
        getComments();
    }

    @Subscribe
    public void onUserIdentitySaved(UserIdentitySavedEvent event) {
        getComments();
    }

    @Subscribe
    public void onVoteSubmitted(VoteSubmittedEvent event) {
        Votable listing = event.getListing();

        if (event.isFailed()) {
            mLinkCommentsView.showToast(R.string.vote_failed);
            return;
        }

        listing.applyVote(event.getDirection());
        if (listing instanceof Link) {
            mLinkCommentsView.linkUpdated();
        } else {
            mLinkCommentsView.commentUpdatedAt(mCommentBank.visibleIndexOf(((AbsComment) listing)));
        }
    }

    @Subscribe
    public void onSaveSubmitted(SaveSubmittedEvent event) {
        Savable listing = event.getListing();

        if (event.isFailed()) {
            mLinkCommentsView.showToast(R.string.save_failed);
            return;
        }

        listing.isSaved(event.isToSave());
        if (listing instanceof Link) {
            mLinkCommentsView.linkUpdated();
        } else {
            mLinkCommentsView.commentUpdatedAt(mCommentBank.visibleIndexOf(((AbsComment) listing)));
        }
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
            mLinkCommentsView.showToast(R.string.listing_archived);
        } else {
            mLinkCommentsView.openReplyView(comment);
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
        if (!mIdentityManager.isUserAuthorized()) {
            mLinkCommentsView.showToast(R.string.user_required);
            return;
        }

        Comment comment = (Comment) mListingSelected;
        mBus.post(new SaveEvent(comment, null, true));
    }

    @Override
    public void unsaveComment() {
        if (!mIdentityManager.isUserAuthorized()) {
            mLinkCommentsView.showToast(R.string.user_required);
            return;
        }

        Comment comment = (Comment) mListingSelected;
        mBus.post(new SaveEvent(comment, null, false));
    }

    @Override
    public void shareComment() {
        Comment comment = (Comment) mListingSelected;
        mLinkCommentsView.openShareView(comment);
    }

    @Override
    public void openCommentUserProfile() {
        Comment comment = (Comment) mListingSelected;
        mLinkCommentsView.openUserProfileView(comment);
    }

    @Override
    public void openCommentUserProfile(Comment comment) {
        mLinkCommentsView.openUserProfileView(comment);
    }

    @Override
    public void openCommentInBrowser() {
        Comment comment = (Comment) mListingSelected;
        mLinkCommentsView.openCommentInBrowser(comment);
    }

    @Override
    public void reportComment() {
        if (!mIdentityManager.isUserAuthorized()) {
            mLinkCommentsView.showToast(R.string.user_required);
            return;
        }

        Comment comment = (Comment) mListingSelected;
        mLinkCommentsView.showToast(R.string.implementation_pending);
    }

    @Override
    public void openCommentLink(Comment comment) {
        // Link is already being displayed with this presenter
    }

    private void vote(int dir) {
        Listing listing = mListingSelected;
        if (((Archivable) listing).isArchived()) {
            mLinkCommentsView.showToast(R.string.listing_archived);
        } else if (!mIdentityManager.isUserAuthorized()) {
            mLinkCommentsView.showToast(R.string.user_required);
        } else {
            Votable votable = (Votable) listing;
            mBus.post(new VoteEvent(votable, listing.getKind(), dir));
        }
    }
}
