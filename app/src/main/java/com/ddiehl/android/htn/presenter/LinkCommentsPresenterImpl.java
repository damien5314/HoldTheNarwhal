package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;

import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.model.CommentBank;
import com.ddiehl.android.htn.model.CommentBankList;
import com.ddiehl.android.htn.view.LinkCommentsView;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.reddit.Archivable;
import com.ddiehl.reddit.CommentUtils;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.AbsComment;
import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.CommentStub;
import com.ddiehl.reddit.listings.Link;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.ListingResponse;
import com.ddiehl.reddit.listings.MoreChildrenResponse;

import java.util.List;

import rx.functions.Action1;

public class LinkCommentsPresenterImpl extends BaseListingsPresenter
    implements LinkCommentsPresenter, IdentityManager.Callbacks {
  private static final int MAX_CHILDREN_PER_REQUEST = 20;

  private LinkCommentsView mLinkCommentsView;
  private CommentBank mCommentBank;
  private Link mLinkContext;
  private Listing mReplyTarget = null;
  private String mLinkId;
  private String mCommentId;

  public LinkCommentsPresenterImpl(
      MainView main, LinkCommentsView view, String subreddit, String linkId, String commentId) {
    super(main, view, view, view, null, null, null, null, subreddit, null, null);
    mLinkCommentsView = view;
    mCommentBank = new CommentBankList();
    mLinkId = linkId;
    mCommentId = commentId;
    mSort = mSettingsManager.getCommentSort();
  }

  @Override
  public void onViewDestroyed() {
    mLinkContext = null;
    mListingSelected = null;
    mCommentBank.clear();
    mListingsView.notifyDataSetChanged();
  }

  @Override
  void requestPreviousData() {

  }

  @Override
  void requestNextData() {
    mMainView.showSpinner(null);
    mRedditService.loadLinkComments(mSubreddit, mLinkId, mSort, mCommentId)
        .doOnTerminate(mMainView::dismissSpinner)
        .subscribe(showLinkComments,
            e -> mMainView.showError(e, R.string.error_get_link_comments));
    mAnalytics.logLoadLinkComments(mSort);
  }

  private Action1<List<ListingResponse>> showLinkComments =
      listingResponseList -> {
        // Link is responseList.get(0), comments are responseList.get(1)
        if (listingResponseList == null) return;
        ListingResponse linkResponse = listingResponseList.get(0);
        mLinkContext = (Link) linkResponse.getData().getChildren().get(0);
        if (mLinkContext != null) mMainView.setTitle(mLinkContext.getTitle());
        ListingResponse commentsResponse = listingResponseList.get(1);
        List<Listing> comments = commentsResponse.getData().getChildren();
        CommentUtils.flattenCommentList(comments);
        mCommentBank.clear();
        mCommentBank.addAll(comments);
        Integer minScore = mSettingsManager.getMinCommentScore();
        mCommentBank.collapseAllThreadsUnder(minScore);
        // TODO Specify commentsAdded
        mListingsView.notifyDataSetChanged();
      };

  @Override
  public Action1<UserIdentity> onUserIdentityChanged() {
    return identity -> refreshData();
  }

  @Override
  public void getMoreComments(@NonNull CommentStub parentStub) {
    List<String> children = parentStub.getChildren();
    // Truncate list of children to 20
    children = children.subList(0, Math.min(MAX_CHILDREN_PER_REQUEST, children.size()));
    mRedditService.loadMoreChildren(mLinkContext, parentStub, children, mSort)
        .doOnSubscribe(() -> mMainView.showSpinner(null))
        .doOnTerminate(mMainView::dismissSpinner)
        .subscribe(showMoreComments(parentStub),
            e -> mMainView.showError(e, R.string.error_get_more_comments));
    mAnalytics.logLoadMoreChildren(mSort);
  }

  private Action1<MoreChildrenResponse> showMoreComments(@NonNull CommentStub parentStub) {
    return response -> {
      List<Listing> comments = response.getChildComments();
      if (comments == null || comments.size() == 0) {
        mCommentBank.remove(parentStub);
      } else {
        CommentUtils.setDepthForCommentsList(comments, parentStub.getDepth());
        int stubIndex = mCommentBank.indexOf(parentStub);
        parentStub.removeChildren(comments);
        parentStub.setCount(parentStub.getChildren().size());
        if (parentStub.getCount() == 0) mCommentBank.remove(stubIndex);
        mCommentBank.addAll(stubIndex, comments);
      }
      Integer minScore = mSettingsManager.getMinCommentScore();
      mCommentBank.collapseAllThreadsUnder(minScore);
      // TODO Specify commentRemoved and commentsAdded
      mListingsView.notifyDataSetChanged();
    };
  }

  @Override
  public Link getLinkContext() {
    return mLinkContext;
  }

  // FIXME Need to port the saving of user setting
  public void updateSort(@NonNull String sort) {
    if (!mSort.equals(sort)) {
      mSort = sort;
      mSettingsManager.saveCommentSort(mSort);
      refreshData();
    }
  }

  @Override
  public AbsComment getListingAt(int position) {
    return mCommentBank.getVisibleComment(position);
  }

  @Override
  public void toggleThreadVisible(Comment comment) {
    int before = mCommentBank.getNumVisible();
    int position = mCommentBank.visibleIndexOf(comment);
    mCommentBank.toggleThreadVisible(comment);
    int diff = mCommentBank.getNumVisible() - before;
    mListingsView.notifyItemChanged(position);
    if (diff > 0) {
      mListingsView.notifyItemRangeInserted(position + 1, diff);
    } else { // diff < 0
      mListingsView.notifyItemRangeRemoved(position + 1, diff * -1);
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
  public void replyToLink() {
    if (((Archivable) mListingSelected).isArchived()) {
      mMainView.showToast(R.string.listing_archived);
    } else if (!mAccessTokenManager.isUserAuthorized()) {
      mMainView.showToast(R.string.user_required);
    } else {
      mReplyTarget = mLinkContext;
      mLinkCommentsView.openReplyView(mLinkContext);
    }
  }

  @Override
  public void replyToComment() {
    if (((Archivable) mListingSelected).isArchived()) {
      mMainView.showToast(R.string.listing_archived);
    } else if (!mAccessTokenManager.isUserAuthorized()) {
      mMainView.showToast(R.string.user_required);
    } else {
      mReplyTarget = mListingSelected;
      mLinkCommentsView.openReplyView(mListingSelected);
    }
  }

  @Override
  public void onCommentSubmitted(@NonNull String commentText) {
    String parentId = String.format("%1$s_%2$s", mReplyTarget.getKind(), mReplyTarget.getId());
    mRedditService.addComment(parentId, commentText)
        .subscribe(comment -> {
          mMainView.showToast("Comment successful"); // FIXME Port to strings.xml
          // TODO Optimize this logic, it probably takes a long time in large threads
          int position;
          if (parentId.startsWith("t1_")) { // Comment
            comment.setDepth(((Comment) mReplyTarget).getDepth() + 1);
            position = mCommentBank.indexOf((Comment) mListingSelected) + 1;
          } else {
            comment.setDepth(1);
            position = 0;
          }
          mCommentBank.add(position, comment);
          mLinkCommentsView.notifyItemInserted(
              mCommentBank.visibleIndexOf(comment));
        }, e -> mMainView.showError(e, R.string.error_add_comment));
  }

  @Override
  public void openCommentLink(@NonNull Comment comment) {
    // Link is already being displayed with this presenter
  }

  @Override
  public boolean shouldShowParentLink() {
    return mCommentId != null;
  }
}
