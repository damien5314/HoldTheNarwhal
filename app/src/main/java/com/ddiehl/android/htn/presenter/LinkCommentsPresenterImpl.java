package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;
import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.model.CommentBank;
import com.ddiehl.android.htn.model.CommentBankList;
import com.ddiehl.android.htn.view.LinkCommentsView;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.RedditNavigationView;

import java.util.HashMap;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rxreddit.RxRedditUtil;
import rxreddit.model.AbsComment;
import rxreddit.model.Archivable;
import rxreddit.model.Comment;
import rxreddit.model.CommentStub;
import rxreddit.model.Link;
import rxreddit.model.Listing;
import rxreddit.model.ListingResponse;
import rxreddit.model.MoreChildrenResponse;
import rxreddit.model.UserIdentity;

public class LinkCommentsPresenterImpl extends BaseListingsPresenter
    implements LinkCommentsPresenter, IdentityManager.Callbacks {

  private static final int MAX_CHILDREN_PER_REQUEST = 20;

  private final LinkCommentsView mLinkCommentsView;
  private final CommentBank mCommentBank;
  private Link mLinkContext;
  private Listing mReplyTarget = null;

  public LinkCommentsPresenterImpl(MainView main, RedditNavigationView navigationView, LinkCommentsView view) {
    super(main, navigationView, view, view, view, null, null);
    mLinkCommentsView = view;
    mCommentBank = new CommentBankList();
  }

  @Override
  public void onResume() {
    mIdentityManager.registerUserIdentityChangeListener(this);
    if (mCommentBank.size() == 0) {
      getNextData();
    }
  }

  @Override
  public void onViewDestroyed() {
    mLinkContext = null;
    mListingSelected = null;
    mCommentBank.clear();
    mLinkCommentsView.notifyDataSetChanged();
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

  }

  @Override
  void requestNextData() {
    mMainView.showSpinner(null);
    mRedditService.loadLinkComments(
        mLinkCommentsView.getSubreddit(), mLinkCommentsView.getArticleId(),
        mLinkCommentsView.getSort(), mLinkCommentsView.getCommentId()
    )
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnTerminate(mMainView::dismissSpinner)
        .subscribe(showLinkComments(),
            e -> {
              String message = mContext.getString(R.string.error_get_link_comments);
              mMainView.showError(e, message);
            });
    mAnalytics.logLoadLinkComments(mLinkCommentsView.getSort());
  }

  private Action1<List<ListingResponse>> showLinkComments() {
    return listingResponseList -> {
      // Link is responseList.get(0), comments are responseList.get(1)
      if (listingResponseList == null) return;
      ListingResponse linkResponse = listingResponseList.get(0);
      mLinkContext = (Link) linkResponse.getData().getChildren().get(0);
      if (mLinkContext != null) mMainView.setTitle(mLinkContext.getTitle());
      ListingResponse commentsResponse = listingResponseList.get(1);
      List<Listing> comments = commentsResponse.getData().getChildren();
      RxRedditUtil.flattenCommentList(comments);
      mCommentBank.clear();
      mCommentBank.addAll(comments);
      Integer minScore = mSettingsManager.getMinCommentScore();
      mCommentBank.collapseAllThreadsUnder(minScore);
      // TODO Specify commentsAdded
      mLinkCommentsView.notifyDataSetChanged();
    };
  }

  @Override
  public Action1<UserIdentity> onUserIdentityChanged() {
    return identity -> refreshData();
  }

  @Override
  public void getMoreComments(@NonNull CommentStub parentStub) {
    List<String> children = parentStub.getChildren();
    // Truncate list of children to 20
    children = children.subList(0, Math.min(MAX_CHILDREN_PER_REQUEST, children.size()));
    mRedditService.loadMoreChildren(mLinkContext.getId(), children, mLinkCommentsView.getSort())
        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe(() -> mMainView.showSpinner(null))
        .doOnTerminate(mMainView::dismissSpinner)
        .subscribe(showMoreComments(parentStub),
            e -> {
              String message = mContext.getString(R.string.error_get_more_comments);
              mMainView.showError(e, message);
            });
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
   *
   * BUG: When we load additional comments, the relative depth is lost somewhere before calling this method
   *   We should account for the depth currently set to the comment when we call this
   */
  public static void setDepthForCommentsList(List<Listing> comments, CommentStub parentStub) {
    HashMap<String, Integer> depthMap = new HashMap<>();
    depthMap.put(parentStub.getId(), parentStub.getDepth());

    for (Listing listing : comments) {
      AbsComment comment = (AbsComment) listing;
      if (depthMap.containsKey(comment.getParentId())) {
        int parentDepth = depthMap.get(comment.getParentId());
        comment.setDepth(parentDepth+1);
      } else {
        comment.setDepth(parentStub.getDepth());
      }
      depthMap.put(comment.getId(), comment.getDepth());
    }
  }

  @Override
  public Link getLinkContext() {
    return mLinkContext;
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
  public void replyToLink() {
    if (((Archivable) mListingSelected).isArchived()) {
      mMainView.showToast(R.string.listing_archived);
    } else if (!mRedditService.isUserAuthorized()) {
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
    } else if (!mRedditService.isUserAuthorized()) {
      mMainView.showToast(R.string.user_required);
    } else {
      mReplyTarget = mListingSelected;
      mLinkCommentsView.openReplyView(mListingSelected);
    }
  }

  @Override
  public void onCommentSubmitted(@NonNull String commentText) {
    String parentId = mReplyTarget.getFullName();
    mRedditService.addComment(parentId, commentText)
        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .subscribe(comment -> {
          String message = mContext.getString(R.string.comment_added);
          mMainView.showToast(message);

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
        }, e -> {
          String message = mContext.getString(R.string.error_add_comment);
          mMainView.showError(e, message);
        });
  }

  @Override
  public void openCommentLink(@NonNull Comment comment) {
    // Link is already being displayed with this presenter
  }

  @Override
  public boolean shouldShowParentLink() {
    return mLinkCommentsView.getCommentId() != null;
  }

  @Override
  public void showLinkContextMenu(ContextMenu menu, View view, Link link) {
    super.showLinkContextMenu(menu, view, link);
    menu.findItem(R.id.action_link_reply).setVisible(true);
  }
}
