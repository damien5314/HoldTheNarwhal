package com.ddiehl.android.htn.presenter;

import android.content.Context;
import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.android.htn.BusProvider;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.RedditIdentityManager;
import com.ddiehl.android.htn.RedditPreferences;
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
import com.ddiehl.reddit.listings.AbsRedditComment;
import com.ddiehl.reddit.listings.CommentBank;
import com.ddiehl.reddit.listings.CommentBankList;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.RedditComment;
import com.ddiehl.reddit.listings.RedditLink;
import com.ddiehl.reddit.listings.RedditMoreComments;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

public class LinkCommentsPresenterImpl implements LinkCommentsPresenter {

    private static final int MAX_CHILDREN_PER_REQUEST = 20;

    private Context mContext;

    private LinkCommentsView mLinkCommentsView;
    private RedditLink mLinkContext;
    private CommentBank mCommentBank;
    private Bus mBus;
    private RedditPreferences mPreferences;

    private RedditIdentityManager mIdentityManager;

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
        mIdentityManager = RedditIdentityManager.getInstance(context);
        mPreferences = RedditPreferences.getInstance(mContext);
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
        AbsRedditComment.Utils.flattenCommentList(comments);
        mCommentBank.clear();
        mCommentBank.addAll(comments);
        mLinkCommentsView.commentsUpdated();
    }

    @Override
    public void getMoreChildren(RedditMoreComments comment) {
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

        RedditMoreComments parentStub = event.getParentStub();
        List<Listing> comments = event.getComments();

        if (comments == null || comments.size() == 0) {
            mCommentBank.remove(parentStub);
        } else {
            AbsRedditComment.Utils.setDepthForCommentsList(comments, parentStub.getDepth());

            // TODO: Pass RedditMoreComments along with event so we don't have to search for it
            for (int i = 0; i < mCommentBank.size(); i++) {
                AbsRedditComment comment = mCommentBank.get(i);
                if (comment instanceof RedditMoreComments) {
                    String id = comment.getId();
                    if (id.equals(parentStub.getId())) { // Found the base comment
                        ((RedditMoreComments) comment).removeChildren(comments);
                        ((RedditMoreComments) comment).setCount(((RedditMoreComments) comment).getChildren().size());
                        if (((RedditMoreComments) comment).getCount() == 0)
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
    public RedditLink getLinkContext() {
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
    public AbsRedditComment getComment(int position) {
        return mCommentBank.getVisibleComment(position);
    }

    @Override
    public void toggleThreadVisible(AbsRedditComment comment) {
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
    public void showLinkContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, RedditLink link) {
        mListingSelected = link;
        mLinkCommentsView.showLinkContextMenu(menu, v, link);
        menu.findItem(R.id.action_link_save).setVisible(!link.isSaved());
        menu.findItem(R.id.action_link_unsave).setVisible(link.isSaved());
    }

    @Override
    public void openLink(RedditLink link) {
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
    public void showCommentsForLink(RedditLink link) {
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
    public void openLinkUserProfile(RedditLink link) {
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
    public void showCommentContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, RedditComment comment) {
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
        if (listing instanceof RedditLink) {
            mLinkCommentsView.linkUpdated();
        } else {
            mLinkCommentsView.commentUpdatedAt(mCommentBank.visibleIndexOf(((AbsRedditComment) listing)));
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
        if (listing instanceof RedditLink) {
            mLinkCommentsView.linkUpdated();
        } else {
            mLinkCommentsView.commentUpdatedAt(mCommentBank.visibleIndexOf(((AbsRedditComment) listing)));
        }
    }

    @Override
    public void openCommentPermalink() {
        RedditComment comment = (RedditComment) mListingSelected;
        showCommentThread(comment.getSubreddit(), comment.getLinkId(), comment.getId());
    }

    @Override
    public void openReplyView() {
        RedditComment comment = (RedditComment) mListingSelected;
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

        RedditComment comment = (RedditComment) mListingSelected;
        mBus.post(new SaveEvent(comment, null, true));
    }

    @Override
    public void unsaveComment() {
        if (!mIdentityManager.isUserAuthorized()) {
            mLinkCommentsView.showToast(R.string.user_required);
            return;
        }

        RedditComment comment = (RedditComment) mListingSelected;
        mBus.post(new SaveEvent(comment, null, false));
    }

    @Override
    public void shareComment() {
        RedditComment comment = (RedditComment) mListingSelected;
        mLinkCommentsView.openShareView(comment);
    }

    @Override
    public void openCommentUserProfile() {
        RedditComment comment = (RedditComment) mListingSelected;
        mLinkCommentsView.openUserProfileView(comment);
    }

    @Override
    public void openCommentUserProfile(RedditComment comment) {
        mLinkCommentsView.openUserProfileView(comment);
    }

    @Override
    public void openCommentInBrowser() {
        RedditComment comment = (RedditComment) mListingSelected;
        mLinkCommentsView.openCommentInBrowser(comment);
    }

    @Override
    public void reportComment() {
        if (!mIdentityManager.isUserAuthorized()) {
            mLinkCommentsView.showToast(R.string.user_required);
            return;
        }

        RedditComment comment = (RedditComment) mListingSelected;
        mLinkCommentsView.showToast(R.string.implementation_pending);
    }

    @Override
    public void openCommentLink(RedditComment comment) {
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
