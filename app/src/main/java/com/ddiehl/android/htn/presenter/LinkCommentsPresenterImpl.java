package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.android.htn.AccessTokenManager;
import com.ddiehl.android.htn.BusProvider;
import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.SettingsManager;
import com.ddiehl.android.htn.analytics.Analytics;
import com.ddiehl.android.htn.events.requests.HideEvent;
import com.ddiehl.android.htn.events.requests.LoadLinkCommentsEvent;
import com.ddiehl.android.htn.events.requests.LoadMoreChildrenEvent;
import com.ddiehl.android.htn.events.requests.SaveEvent;
import com.ddiehl.android.htn.events.requests.VoteEvent;
import com.ddiehl.android.htn.events.responses.LinkCommentsLoadedEvent;
import com.ddiehl.android.htn.events.responses.MoreChildrenLoadedEvent;
import com.ddiehl.android.htn.events.responses.SaveSubmittedEvent;
import com.ddiehl.android.htn.events.responses.VoteSubmittedEvent;
import com.ddiehl.android.htn.model.CommentBank;
import com.ddiehl.android.htn.model.CommentBankList;
import com.ddiehl.android.htn.view.LinkCommentsView;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.reddit.Archivable;
import com.ddiehl.reddit.Savable;
import com.ddiehl.reddit.Votable;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.AbsComment;
import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.CommentStub;
import com.ddiehl.reddit.listings.Link;
import com.ddiehl.reddit.listings.Listing;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import rx.functions.Action1;

public class LinkCommentsPresenterImpl
        implements LinkCommentsPresenter, IdentityManager.Callbacks {
    private static final int MAX_CHILDREN_PER_REQUEST = 20;

    private MainView mMainView;
    private LinkCommentsView mLinkCommentsView;
    private Link mLinkContext;
    private CommentBank mCommentBank;

    private Bus mBus = BusProvider.getInstance();
    private AccessTokenManager mAccessTokenManager = HoldTheNarwhal.getAccessTokenManager();
    private SettingsManager mSettingsManager = HoldTheNarwhal.getSettingsManager();
    private Analytics mAnalytics = HoldTheNarwhal.getAnalytics();

    private String mSubreddit;
    private String mLinkId;
    private String mCommentId;
    private String mSort; // Remove this and read from preferences when needed

    private Listing mListingSelected;

    public LinkCommentsPresenterImpl(
            MainView main, LinkCommentsView view, String subreddit, String linkId, String commentId) {
        mMainView = main;
        mLinkCommentsView = view;
        mCommentBank = new CommentBankList();
        mSubreddit = subreddit;
        mLinkId = linkId;
        mCommentId = commentId;
        mSort = mSettingsManager.getCommentSort();
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void requestData() {
        mMainView.showSpinner(null);
        mBus.post(new LoadLinkCommentsEvent(mSubreddit, mLinkId, mSort, mCommentId));
        mAnalytics.logLoadLinkComments(mSort);
    }

    @Override
    public Action1<UserIdentity> onUserIdentityChanged() {
        return identity -> requestData();
    }

    @Subscribe @SuppressWarnings("unused")
    public void onCommentsLoaded(LinkCommentsLoadedEvent event) {
        mMainView.dismissSpinner();
        if (event.isFailed()) {
            return;
        }

        mLinkContext = event.getLink();
        if (mLinkContext != null) mMainView.setTitle(mLinkContext.getTitle());
        List<Listing> comments = event.getComments();
        AbsComment.Utils.flattenCommentList(comments);
        mCommentBank.clear();
        mCommentBank.addAll(comments);
        Integer minScore = mSettingsManager.getMinCommentScore();
        mCommentBank.collapseAllThreadsUnder(minScore);
        mLinkCommentsView.commentsUpdated();
    }

    @Override
    public void getMoreChildren(@NonNull CommentStub comment) {
        mMainView.showSpinner(null);
        List<String> children = comment.getChildren();
        // Truncate list of children to 20
        children = children.subList(0, Math.min(MAX_CHILDREN_PER_REQUEST, children.size()));
        mBus.post(new LoadMoreChildrenEvent(mLinkContext, comment, children, mSort));
        mAnalytics.logLoadMoreChildren(mSort);
    }

    @Subscribe @SuppressWarnings("unused")
    public void onMoreChildrenLoaded(MoreChildrenLoadedEvent event) {
        mMainView.dismissSpinner();
        if (event.isFailed()) {
            return;
        }

        CommentStub parentStub = event.getParentStub();
        List<Listing> comments = event.getComments();

        if (comments == null || comments.size() == 0) {
            mCommentBank.remove(parentStub);
        } else {
            if (parentStub != null) {
                AbsComment.Utils.setDepthForCommentsList(comments, parentStub.getDepth());
                int stubIndex = mCommentBank.indexOf(parentStub);
                parentStub.removeChildren(comments);
                parentStub.setCount(parentStub.getChildren().size());
                if (parentStub.getCount() == 0)
                    mCommentBank.remove(stubIndex);
                mCommentBank.addAll(stubIndex, comments);
            }
        }

        Integer minScore = mSettingsManager.getMinCommentScore();
        mCommentBank.collapseAllThreadsUnder(minScore);
        mLinkCommentsView.commentsUpdated();
    }

    @Override
    public Link getLinkContext() {
        return mLinkContext;
    }

    @Override
    public void updateSort(@NonNull String sort) {
        if (!mSort.equals(sort)) {
            mSort = sort;
            mSettingsManager.saveCommentSort(mSort);
            requestData();
        }
    }

    @Override
    public AbsComment getComment(int position) {
        return mCommentBank.getVisibleComment(position);
    }

    @Override
    public void toggleThreadVisible(@NonNull AbsComment comment) {
        mCommentBank.toggleThreadVisible(comment);
        mLinkCommentsView.commentsUpdated();
    }

    @Override
    public String getSort() {
        return mSort;
    }

    @Override
    public boolean getShowControversiality() {
        return mSettingsManager.getShowControversiality();
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
    public void openLink(@NonNull Link link) {
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
    public void showCommentsForLink(@NonNull Link link) {
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
        if (!mAccessTokenManager.isUserAuthorized()) {
            mMainView.showToast(R.string.user_required);
            return;
        }

        mBus.post(new SaveEvent(mLinkContext, null, true));
        mAnalytics.logSave(mLinkContext.getKind(), null, true);
    }

    @Override
    public void unsaveLink() {
        if (!mAccessTokenManager.isUserAuthorized()) {
            mMainView.showToast(R.string.user_required);
            return;
        }

        mBus.post(new SaveEvent(mLinkContext, null, false));
        mAnalytics.logSave(mLinkContext.getKind(), null, false);
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
    public void openLinkUserProfile(@NonNull Link link) {
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
        if (!mAccessTokenManager.isUserAuthorized()) {
            mMainView.showToast(R.string.user_required);
            return;
        }

        mBus.post(new HideEvent(mLinkContext, true));
        mAnalytics.logHide(mLinkContext.getKind(), true);
    }

    @Override
    public void unhideLink() {
        if (!mAccessTokenManager.isUserAuthorized()) {
            mMainView.showToast(R.string.user_required);
            return;
        }

        mBus.post(new HideEvent(mLinkContext, false));
        mAnalytics.logHide(mLinkContext.getKind(), false);
    }

    @Override
    public void reportLink() {
        if (!mAccessTokenManager.isUserAuthorized()) {
            mMainView.showToast(R.string.user_required);
            return;
        }

        mMainView.showToast(R.string.implementation_pending);
    }

    @Override
    public void showCommentContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Comment comment) {
        mListingSelected = comment;
        mLinkCommentsView.showCommentContextMenu(menu, v, comment);
        menu.findItem(R.id.action_comment_save).setVisible(!comment.isSaved());
        menu.findItem(R.id.action_comment_unsave).setVisible(comment.isSaved());
    }

    @Override
    public void showCommentThread(@Nullable String subreddit, @Nullable String linkId, @NonNull String commentId) {
        // Calls from a ThreadStubViewHolder will not have subreddit or linkId
        // so only set if it's not null
        mSubreddit = subreddit == null ? mSubreddit : subreddit;
        mCommentId = commentId.contains("_") ? commentId.substring(3) : commentId; // Remove type prefix
        mLinkCommentsView.showCommentsForLink(mSubreddit, mLinkId, mCommentId);
    }

    @Subscribe @SuppressWarnings("unused")
    public void onVoteSubmitted(VoteSubmittedEvent event) {
        Votable listing = event.getListing();

        if (event.isFailed()) {
            mMainView.showToast(R.string.vote_failed);
            return;
        }

        if (listing != null) listing.applyVote(event.getDirection());
        if (listing instanceof Link) {
            mLinkCommentsView.linkUpdated();
        } else {
            mLinkCommentsView.commentUpdatedAt(mCommentBank.visibleIndexOf(((AbsComment) listing)));
        }
    }

    @Subscribe @SuppressWarnings("unused")
    public void onSaveSubmitted(SaveSubmittedEvent event) {
        Savable listing = event.getListing();

        if (event.isFailed()) {
            mMainView.showToast(R.string.save_failed);
            return;
        }

        if (listing != null) listing.isSaved(event.isToSave());
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
            mMainView.showToast(R.string.listing_archived);
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
        if (!mAccessTokenManager.isUserAuthorized()) {
            mMainView.showToast(R.string.user_required);
            return;
        }

        Comment comment = (Comment) mListingSelected;
        mBus.post(new SaveEvent(comment, null, true));
        mAnalytics.logSave(comment.getKind(), null, true);
    }

    @Override
    public void unsaveComment() {
        if (!mAccessTokenManager.isUserAuthorized()) {
            mMainView.showToast(R.string.user_required);
            return;
        }

        Comment comment = (Comment) mListingSelected;
        mBus.post(new SaveEvent(comment, null, false));
        mAnalytics.logSave(comment.getKind(), null, false);
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
    public void openCommentUserProfile(@NonNull Comment comment) {
        mLinkCommentsView.openUserProfileView(comment);
    }

    @Override
    public void openCommentInBrowser() {
        Comment comment = (Comment) mListingSelected;
        mLinkCommentsView.openCommentInBrowser(comment);
    }

    @Override
    public void reportComment() {
        if (!mAccessTokenManager.isUserAuthorized()) {
            mMainView.showToast(R.string.user_required);
            return;
        }

        Comment comment = (Comment) mListingSelected;
        mMainView.showToast(R.string.implementation_pending);
    }

    @Override
    public void openCommentLink(@NonNull Comment comment) {
        // Link is already being displayed with this presenter
    }

    private void vote(int dir) {
        Listing listing = mListingSelected;
        if (((Archivable) listing).isArchived()) {
            mMainView.showToast(R.string.listing_archived);
        } else if (!mAccessTokenManager.isUserAuthorized()) {
            mMainView.showToast(R.string.user_required);
        } else {
            Votable votable = (Votable) listing;
            mBus.post(new VoteEvent(votable, listing.getKind(), dir));
            mAnalytics.logVote(votable.getKind(), dir);
        }
    }
}
