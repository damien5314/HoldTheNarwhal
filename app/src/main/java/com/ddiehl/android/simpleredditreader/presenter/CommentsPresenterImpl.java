package com.ddiehl.android.simpleredditreader.presenter;

import android.content.Context;
import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.android.simpleredditreader.BusProvider;
import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.RedditPreferences;
import com.ddiehl.android.simpleredditreader.events.requests.LoadCommentsEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadMoreChildrenEvent;
import com.ddiehl.android.simpleredditreader.events.requests.SaveEvent;
import com.ddiehl.android.simpleredditreader.events.requests.VoteEvent;
import com.ddiehl.android.simpleredditreader.events.responses.CommentsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.MoreChildrenLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.SaveSubmittedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.UserIdentitySavedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.VoteSubmittedEvent;
import com.ddiehl.android.simpleredditreader.view.CommentsView;
import com.ddiehl.reddit.Savable;
import com.ddiehl.reddit.Votable;
import com.ddiehl.reddit.listings.AbsRedditComment;
import com.ddiehl.reddit.listings.CommentBank;
import com.ddiehl.reddit.listings.CommentBankList;
import com.ddiehl.reddit.listings.RedditComment;
import com.ddiehl.reddit.listings.RedditLink;
import com.ddiehl.reddit.listings.RedditMoreComments;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

public class CommentsPresenterImpl implements CommentsPresenter {

    private Context mContext;

    private CommentsView mCommentsView;
    private RedditLink mRedditLink;
    private CommentBank mCommentBank;
    private Bus mBus;
    private RedditPreferences mPreferences;

    private String mSubreddit;
    private String mArticleId;
    private String mCommentId;
    private String mSort;

    private RedditComment mCommentSelected;

    public CommentsPresenterImpl(Context context, CommentsView commentsView,
                                 String subreddit, String articleId, String commentId) {
        mContext = context.getApplicationContext();
        mCommentsView = commentsView;
        mCommentBank = new CommentBankList();
        mBus = BusProvider.getInstance();

        mPreferences = RedditPreferences.getInstance(mContext);

        mSubreddit = subreddit;
        mArticleId = articleId;
        mCommentId = commentId;
        mSort = mPreferences.getCommentSort();
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
    public void onUserIdentitySaved(UserIdentitySavedEvent event) {
        getComments();
    }

    @Subscribe
    public void onCommentsLoaded(CommentsLoadedEvent event) {
        mCommentsView.dismissSpinner();
        if (event.isFailed()) {
            return;
        }

        mRedditLink = event.getLink();

        List<AbsRedditComment> comments = event.getComments();
        AbsRedditComment.flattenCommentList(comments);
        mCommentBank.setData(comments);
        mCommentsView.commentsUpdated();
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
