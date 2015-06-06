package com.ddiehl.android.simpleredditreader.presenter;

import android.content.Context;
import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.android.simpleredditreader.BusProvider;
import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.RedditPreferences;
import com.ddiehl.android.simpleredditreader.events.requests.HideEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadLinkCommentsEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadMoreChildrenEvent;
import com.ddiehl.android.simpleredditreader.events.requests.SaveEvent;
import com.ddiehl.android.simpleredditreader.events.requests.VoteEvent;
import com.ddiehl.android.simpleredditreader.events.responses.LinkCommentsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.MoreChildrenLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.SaveSubmittedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.UserIdentitySavedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.VoteSubmittedEvent;
import com.ddiehl.android.simpleredditreader.view.LinkCommentsView;
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

public class LinkCommentsPresenterImpl implements LinkCommentsPresenter {
    private static final String TAG = LinkCommentsPresenterImpl.class.getSimpleName();

    private Context mContext;

    private LinkCommentsView mLinkCommentsView;
    private RedditLink mLinkContext;
    private CommentBank mCommentBank;
    private Bus mBus;
    private RedditPreferences mPreferences;

    private String mSubreddit;
    private String mArticleId;
    private String mCommentId;
    private String mSort; // Remove this and read from preferences when needed

    private RedditComment mCommentSelected;

    public LinkCommentsPresenterImpl(Context context, LinkCommentsView view,
                                     String subreddit, String articleId, String commentId) {
        mContext = context.getApplicationContext();
        mLinkCommentsView = view;
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
        List<AbsRedditComment> comments = event.getComments();
        AbsRedditComment.Utils.flattenCommentList(comments);
        mCommentBank.clear();
        mCommentBank.addAll(comments);
        mLinkCommentsView.commentsUpdated();
    }

    @Subscribe
    public void onMoreChildrenLoaded(MoreChildrenLoadedEvent event) {
        mLinkCommentsView.dismissSpinner();
        if (event.isFailed()) {
            return;
        }

        RedditMoreComments parentStub = event.getParentStub();
        List<AbsRedditComment> comments = event.getComments();

        if (comments.size() == 0) {
            mCommentBank.remove(parentStub);
        } else {
            AbsRedditComment.Utils.setDepthForCommentsList(comments, parentStub.getDepth());

            for (int i = 0; i < mCommentBank.size(); i++) {
                AbsRedditComment comment = (AbsRedditComment) mCommentBank.get(i);
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

        mLinkCommentsView.commentsUpdated();
    }

    @Override
    public RedditLink getLinkContext() {
        return mLinkContext;
    }

    @Override
    public void setLinkContext(RedditLink link) {
        mLinkContext = link;
    }

    @Override
    public void getMoreChildren(RedditMoreComments comment) {
        mLinkCommentsView.showSpinner(null);
        List<String> children = comment.getChildren();
        mBus.post(new LoadMoreChildrenEvent(mLinkContext, comment, children, mSort));
    }

    @Override
    public void updateSort() {
        String sort = mContext.getSharedPreferences(RedditPreferences.PREFS_USER, Context.MODE_PRIVATE)
                .getString(RedditPreferences.PREF_COMMENT_SORT, mContext.getString(R.string.default_comment_sort));
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
        mLinkCommentsView.showLinkContextMenu(menu, v, menuInfo, link);
        menu.findItem(R.id.action_link_save).setVisible(!link.isSaved());
        menu.findItem(R.id.action_link_unsave).setVisible(link.isSaved());
    }

    @Override
    public void openLink(RedditLink link) {
        if (link == null)
            return;

        if (link.isSelf()) {
            mLinkCommentsView.showCommentsForLink(link);
        } else {
            mLinkCommentsView.openWebViewForLink(link);
        }
    }

    @Override
    public void showCommentsForLink() {
        mLinkCommentsView.showCommentsForLink(mLinkContext);
    }

    @Override
    public void showCommentsForLink(RedditLink link) {
        mLinkCommentsView.showCommentsForLink(link);
    }

    @Override
    public void upvoteLink() {
        if (mLinkContext.isArchived()) {
            mLinkCommentsView.showToast(R.string.listing_archived);
        } else {
            int dir = (mLinkContext.isLiked() == null || !mLinkContext.isLiked()) ? 1 : 0;
            mBus.post(new VoteEvent(mLinkContext, mLinkContext.getKind(), dir));
        }
    }

    @Override
    public void downvoteLink() {
        if (mLinkContext.isArchived()) {
            mLinkCommentsView.showToast(R.string.listing_archived);
        } else {
            int dir = (mLinkContext.isLiked() == null || mLinkContext.isLiked()) ? -1 : 0;
            mBus.post(new VoteEvent(mLinkContext, mLinkContext.getKind(), dir));
        }
    }

    @Override
    public void saveLink() {
        mBus.post(new SaveEvent(mLinkContext, null, true));
    }

    @Override
    public void unsaveLink() {
        mBus.post(new SaveEvent(mLinkContext, null, false));
    }

    @Override
    public void shareLink() {
        mLinkCommentsView.openShareView(mLinkContext);
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
        mBus.post(new HideEvent(mLinkContext, true));
    }

    @Override
    public void unhideLink() {
        mBus.post(new HideEvent(mLinkContext, false));
    }

    @Override
    public void reportLink() {
        mLinkCommentsView.showToast(R.string.implementation_pending);
    }

    @Override
    public void showCommentContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, RedditComment comment) {
        mCommentSelected = comment;
        mLinkCommentsView.showCommentContextMenu(menu, v, menuInfo, comment);

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
            mLinkCommentsView.commentUpdatedAt(mCommentBank.indexOf(listing));
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
            mLinkCommentsView.commentUpdatedAt(mCommentBank.indexOf(listing));
        }
    }

    @Override
    public void openReplyView() {
        RedditComment comment = mCommentSelected;
        if (comment.isArchived()) {
            mLinkCommentsView.showToast(R.string.listing_archived);
        } else {
            mLinkCommentsView.openReplyView(comment);
        }
    }

    @Override
    public void upvoteComment() {
        RedditComment comment = mCommentSelected;
        if (comment.isArchived()) {
            mLinkCommentsView.showToast(R.string.listing_archived);
        } else {
            int dir = (comment.isLiked() == null || !comment.isLiked()) ? 1 : 0;
            mBus.post(new VoteEvent(comment, "t1", dir));
        }
    }

    @Override
    public void downvoteComment() {
        RedditComment comment = mCommentSelected;
        if (comment.isArchived()) {
            mLinkCommentsView.showToast(R.string.listing_archived);
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
        mLinkCommentsView.openShareView(comment);
    }

    @Override
    public void openCommentInBrowser() {
        RedditComment comment = mCommentSelected;
        mLinkCommentsView.openCommentInBrowser(comment);
    }

    @Override
    public void reportComment() {
        RedditComment comment = mCommentSelected;
        mLinkCommentsView.showToast(R.string.implementation_pending);
    }
}
