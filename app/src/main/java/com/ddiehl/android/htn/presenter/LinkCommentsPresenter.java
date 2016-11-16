package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.model.CommentBank;
import com.ddiehl.android.htn.model.CommentBankList;
import com.ddiehl.android.htn.view.LinkCommentsView;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.RedditNavigationView;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rxreddit.RxRedditUtil;
import rxreddit.model.AbsComment;
import rxreddit.model.Comment;
import rxreddit.model.CommentStub;
import rxreddit.model.Link;
import rxreddit.model.Listing;
import rxreddit.model.ListingResponse;
import rxreddit.model.MoreChildrenResponse;
import timber.log.Timber;

public class LinkCommentsPresenter extends BaseListingsPresenter {

    private static final int MAX_CHILDREN_PER_REQUEST = 20;

    private final LinkCommentsView mLinkCommentsView;
    private final CommentBank mCommentBank;
    private Link mLinkContext;
    private Listing mReplyTarget = null;
    private boolean mDataRequested = false;

    public LinkCommentsPresenter(MainView main, RedditNavigationView navigationView, LinkCommentsView view) {
        super(main, navigationView, view, view, view, null);
        mLinkCommentsView = view;
        mCommentBank = new CommentBankList();
    }

    @Override
    public boolean hasData() {
        return mCommentBank.size() != 0;
    }

    @Override
    public void clearData() {
        mLinkContext = null;
        mCommentBank.clear();
    }

    @Override
    public void refreshData() {
        int numItems = mListings.size();
        mCommentBank.clear();
        mLinkCommentsView.notifyItemRangeRemoved(0, numItems);
        getNextData();
    }

    @Override
    void requestPreviousData() {
        // Comments aren't paginated the same as a listings endpoint
    }

    @Override
    void requestNextData() {
        if (!mDataRequested) {
            mDataRequested = true;
            mRedditService.loadLinkComments(
                    mLinkCommentsView.getSubreddit(), mLinkCommentsView.getArticleId(),
                    mLinkCommentsView.getSort(), mLinkCommentsView.getCommentId()
            )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(mMainView::showSpinner)
                    .doOnTerminate(() -> {
                        mDataRequested = false;
                        mMainView.dismissSpinner();
                    })
                    .subscribe(
                            showLinkComments(),
                            error -> {
                                if (error instanceof IOException) {
                                    String message = mContext.getString(R.string.error_network_unavailable);
                                    mMainView.showError(message);
                                } else {
                                    Timber.w(error, "Error retrieving comment listings");
                                    String message = mContext.getString(R.string.error_get_link_comments);
                                    mMainView.showError(message);
                                }
                            }
                    );
            mAnalytics.logLoadLinkComments(mLinkCommentsView.getSort());
        }
    }

    private Action1<List<ListingResponse>> showLinkComments() {
        return listingResponseList -> {
            if (listingResponseList == null) return;

            mLinkCommentsView.refreshOptionsMenu();

            // Get link
            ListingResponse linkResponse = listingResponseList.get(0);
            mLinkContext = (Link) linkResponse.getData().getChildren().get(0);

            // Get comments
            ListingResponse commentsResponse = listingResponseList.get(1);
            List<Listing> comments = commentsResponse.getData().getChildren();

            // Flatten the returned comment tree
            RxRedditUtil.flattenCommentList(comments);

            // Add comments to CommentBank
            mCommentBank.clear();
            mCommentBank.addAll(comments);

            // Collapse all threads under the user's minimum score
            Integer minScore = mSettingsManager.getMinCommentScore();
            mCommentBank.collapseAllThreadsUnder(minScore);

            // Notify adapter
            // TODO Specify commentsAdded
            mLinkCommentsView.notifyDataSetChanged();
        };
    }

    @Override
    public void getMoreComments(@NonNull CommentStub parentStub) {
        List<String> children = parentStub.getChildren();
        // Truncate list of children to 20
        children = children.subList(0, Math.min(MAX_CHILDREN_PER_REQUEST, children.size()));
        mRedditService.loadMoreChildren(mLinkContext.getId(), children, mLinkCommentsView.getSort())
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(mMainView::showSpinner)
                .doOnTerminate(mMainView::dismissSpinner)
                .subscribe(
                        showMoreComments(parentStub),
                        error -> {
                            if (error instanceof IOException) {
                                String message = mContext.getString(R.string.error_network_unavailable);
                                mMainView.showError(message);
                            } else {
                                Timber.w(error, "Error retrieving more comments");
                                String message = mContext.getString(R.string.error_get_more_comments);
                                mMainView.showError(message);
                            }
                        }
                );
        mAnalytics.logLoadMoreChildren(mLinkCommentsView.getSort());
    }

    private Action1<MoreChildrenResponse> showMoreComments(@NonNull CommentStub parentStub) {
        return response -> {
            List<Listing> comments = response.getChildComments();
            if (comments == null || comments.size() == 0) {
                mCommentBank.remove(parentStub);
            } else {
                setDepthForCommentsList(comments, parentStub);
                int stubIndex = mCommentBank.indexOf(parentStub);
                parentStub.removeChildren(comments);
                parentStub.setCount(parentStub.getChildren().size());
                if (parentStub.getCount() == 0) mCommentBank.remove(stubIndex);
                mCommentBank.addAll(stubIndex, comments);
            }
            Integer minScore = mSettingsManager.getMinCommentScore();
            mCommentBank.collapseAllThreadsUnder(minScore);
            // TODO Specify commentRemoved and commentsAdded
            mLinkCommentsView.notifyDataSetChanged();
        };
    }

    /**
     * Sets depth for comments in a flat comments list
     * <p>
     * BUG: When we load additional comments, the relative depth is lost somewhere before calling this method
     * We should account for the depth currently set to the comment when we call this
     */
    public static void setDepthForCommentsList(List<Listing> comments, CommentStub parentStub) {
        HashMap<String, Integer> depthMap = new HashMap<>();
        depthMap.put(parentStub.getId(), parentStub.getDepth());

        for (Listing listing : comments) {
            AbsComment comment = (AbsComment) listing;
            if (depthMap.containsKey(comment.getParentId())) {
                int parentDepth = depthMap.get(comment.getParentId());
                comment.setDepth(parentDepth + 1);
            } else {
                comment.setDepth(parentStub.getDepth());
            }
            depthMap.put(comment.getId(), comment.getDepth());
        }
    }

    public Link getLinkContext() {
        return mLinkContext;
    }

    @Override
    public AbsComment getListingAt(int position) {
        return mCommentBank.getVisibleComment(position);
    }

    public void toggleThreadVisible(Comment comment) {
        int before = mCommentBank.getNumVisible();
        int position = mCommentBank.visibleIndexOf(comment);
        mCommentBank.toggleThreadVisible(comment);
        int diff = mCommentBank.getNumVisible() - before;
        mLinkCommentsView.notifyItemChanged(position);
        if (diff > 0) {
            mLinkCommentsView.notifyItemRangeInserted(position + 1, diff);
        } else { // diff < 0
            mLinkCommentsView.notifyItemRangeRemoved(position + 1, diff * -1);
        }
    }

    @Override
    public int getNumListings() {
        return mCommentBank.getNumVisible();
    }

    @Override
    protected int getIndexOf(Listing listing) {
        if (listing instanceof AbsComment)
            return mCommentBank.visibleIndexOf((AbsComment) listing);
        else return -1;
    }

    @Override
    public void openLink(@NonNull Link link) {
        // Overriding this so we don't keep opening link comments over and over
        if (!link.isSelf()) {
            mLinkCommentsView.openLinkInWebView(link);
        }
    }

    @Override
    public void replyToLink(Link link) {
        if (link.isArchived()) {
            mMainView.showToast(mContext.getString(R.string.listing_archived));
        } else if (!mRedditService.isUserAuthorized()) {
            mMainView.showToast(mContext.getString(R.string.user_required));
        } else {
            mReplyTarget = link;
            mLinkCommentsView.openReplyView(link);
        }
    }

    @Override
    public void replyToComment(@NonNull Comment comment) {
        if (comment.isArchived()) {
            mMainView.showToast(mContext.getString(R.string.listing_archived));
        } else if (!mRedditService.isUserAuthorized()) {
            mMainView.showToast(mContext.getString(R.string.user_required));
        } else {
            mReplyTarget = comment;
            mLinkCommentsView.openReplyView(comment);
        }
    }

    public void onCommentSubmitted(@NonNull String commentText) {
        String parentId = mReplyTarget.getFullName();
        mRedditService.addComment(parentId, commentText)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        comment -> {
                            String message = mContext.getString(R.string.comment_added);
                            mMainView.showToast(message);

                            // TODO Optimize this logic, it probably takes a long time in large threads
                            int position;
                            if (parentId.startsWith("t1_")) { // Comment
                                comment.setDepth(((Comment) mReplyTarget).getDepth() + 1);
                                position = mCommentBank.indexOf((Comment) mReplyTarget) + 1;
                            } else {
                                comment.setDepth(1);
                                position = 0;
                            }
                            mCommentBank.add(position, comment);
                            mLinkCommentsView.notifyItemInserted(
                                    mCommentBank.visibleIndexOf(comment));
                        },
                        error -> {
                            if (error instanceof IOException) {
                                String message = mContext.getString(R.string.error_network_unavailable);
                                mMainView.showError(message);
                            } else {
                                Timber.w(error, "Error adding comment");
                                String message = mContext.getString(R.string.error_add_comment);
                                mMainView.showError(message);
                            }
                        }
                );
    }

    @Override
    public void openCommentLink(@NonNull Comment comment) {
        // Link is already being displayed with this presenter
    }

    public boolean shouldShowParentLink() {
        return mLinkCommentsView.getCommentId() != null;
    }
}
