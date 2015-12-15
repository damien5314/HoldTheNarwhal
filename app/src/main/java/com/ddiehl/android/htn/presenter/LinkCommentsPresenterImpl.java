package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.android.htn.AccessTokenManager;
import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.SettingsManager;
import com.ddiehl.android.htn.analytics.Analytics;
import com.ddiehl.android.htn.io.RedditService;
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
import com.ddiehl.reddit.listings.ListingResponse;
import com.ddiehl.reddit.listings.MoreChildrenResponse;

import java.util.List;

import rx.functions.Action1;

public class LinkCommentsPresenterImpl
    implements LinkCommentsPresenter, IdentityManager.Callbacks {
  private static final int MAX_CHILDREN_PER_REQUEST = 20;

  private MainView mMainView;
  private LinkCommentsView mLinkCommentsView;
  private Link mLinkContext;
  private CommentBank mCommentBank;

  private RedditService mRedditService = HoldTheNarwhal.getRedditService();
  private AccessTokenManager mAccessTokenManager = HoldTheNarwhal.getAccessTokenManager();
  private IdentityManager mIdentityManager = HoldTheNarwhal.getIdentityManager();
  private SettingsManager mSettingsManager = HoldTheNarwhal.getSettingsManager();
  private Analytics mAnalytics = HoldTheNarwhal.getAnalytics();

  private String mSubreddit;
  private String mLinkId;
  private String mCommentId;
  private String mSort; // Remove this and read from preferences when needed

  private Listing mListingSelected = null;
  private Listing mReplyTarget = null;

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
    mIdentityManager.registerUserIdentityChangeListener(this);
    if (mCommentBank.size() == 0) {
      requestData();
    }
  }

  @Override
  public void onPause() {
    mIdentityManager.unregisterUserIdentityChangeListener(this);
  }

  @Override
  public void requestData() {
    mMainView.showSpinner(null);
    mRedditService.loadLinkComments(mSubreddit, mLinkId, mSort, mCommentId)
        .doOnTerminate(mMainView::dismissSpinner)
        .subscribe(showLinkComments, mMainView::showError);
    mAnalytics.logLoadLinkComments(mSort);
  }

  private Action1<List<ListingResponse>> showLinkComments =
      (listingResponseList) -> {
        // Link is responseList.get(0), comments are responseList.get(1)
        ListingResponse linkResponse = listingResponseList.get(0);
        mLinkContext = (Link) linkResponse.getData().getChildren().get(0);
        if (mLinkContext != null) mMainView.setTitle(mLinkContext.getTitle());
        ListingResponse commentsResponse = listingResponseList.get(1);
        List<Listing> comments = commentsResponse.getData().getChildren();
        AbsComment.Utils.flattenCommentList(comments);
        mCommentBank.clear();
        mCommentBank.addAll(comments);
        Integer minScore = mSettingsManager.getMinCommentScore();
        mCommentBank.collapseAllThreadsUnder(minScore);
        mLinkCommentsView.commentsUpdated();
      };

  @Override
  public Action1<UserIdentity> onUserIdentityChanged() {
    return identity -> requestData();
  }

  @Override
  public void getMoreComments(@NonNull CommentStub parentStub) {
    mMainView.showSpinner(null);
    List<String> children = parentStub.getChildren();
    // Truncate list of children to 20
    children = children.subList(0, Math.min(MAX_CHILDREN_PER_REQUEST, children.size()));
    mRedditService.loadMoreChildren(mLinkContext, parentStub, children, mSort)
        .doOnTerminate(mMainView::dismissSpinner)
        .subscribe(showMoreComments(parentStub), mMainView::showError);
    mAnalytics.logLoadMoreChildren(mSort);
  }

  private Action1<MoreChildrenResponse> showMoreComments(@NonNull CommentStub parentStub) {
    return response -> {
      List<Listing> comments = response.getChildComments();
      if (comments == null || comments.size() == 0) {
        mCommentBank.remove(parentStub);
      } else {
        AbsComment.Utils.setDepthForCommentsList(comments, parentStub.getDepth());
        int stubIndex = mCommentBank.indexOf(parentStub);
        parentStub.removeChildren(comments);
        parentStub.setCount(parentStub.getChildren().size());
        if (parentStub.getCount() == 0) mCommentBank.remove(stubIndex);
        mCommentBank.addAll(stubIndex, comments);
      }
      Integer minScore = mSettingsManager.getMinCommentScore();
      mCommentBank.collapseAllThreadsUnder(minScore);
      mLinkCommentsView.commentsUpdated();
    };
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
  public void showLinkContextMenu(
      ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Link link) {
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
  public void replyToLink() {
    mReplyTarget = mLinkContext;
    mLinkCommentsView.openReplyView(mLinkContext);
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
    save(mLinkContext, true);
    mAnalytics.logSave(mLinkContext.getKind(), null, true);
  }

  @Override
  public void unsaveLink() {
    if (!mAccessTokenManager.isUserAuthorized()) {
      mMainView.showToast(R.string.user_required);
      return;
    }
    save(mLinkContext, false);
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

    hideLink(true);
    mAnalytics.logHide(mLinkContext.getKind(), true);
  }

  @Override
  public void unhideLink() {
    if (!mAccessTokenManager.isUserAuthorized()) {
      mMainView.showToast(R.string.user_required);
      return;
    }

    hideLink(false);
    mAnalytics.logHide(mLinkContext.getKind(), false);
  }

  private void hideLink(boolean toHide) {
    mRedditService.hide(mLinkContext, toHide)
        .subscribe(response -> {
          if (toHide) mMainView.showToast(R.string.link_hidden);
        }, error -> mMainView.showToast(R.string.hide_failed));
  }

  @Override
  public void reportLink() {
    Listing listing = mLinkContext;
    if (((Archivable) listing).isArchived()) {
      mMainView.showToast(R.string.listing_archived);
    } else if (!mAccessTokenManager.isUserAuthorized()) {
      mMainView.showToast(R.string.user_required);
    } else {
      mMainView.showToast(R.string.implementation_pending);
    }
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

  @Override
  public void openCommentPermalink() {
    Comment comment = (Comment) mListingSelected;
    showCommentThread(comment.getSubreddit(), comment.getLinkId(), comment.getId());
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
          mMainView.showToast("Comment successful");
          int position;
          if (parentId.startsWith("t1_")) { // Comment
            comment.setDepth(((Comment) mReplyTarget).getDepth()+1);
            position = mCommentBank.indexOf((Comment) mListingSelected) + 1;
          } else position = 0;
          mCommentBank.add(position, comment);
          mLinkCommentsView.commentAddedAt(position);
        }, mMainView::showError);
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
    save(comment, true);
    mAnalytics.logSave(comment.getKind(), null, true);
  }

  @Override
  public void unsaveComment() {
    if (!mAccessTokenManager.isUserAuthorized()) {
      mMainView.showToast(R.string.user_required);
      return;
    }

    Comment comment = (Comment) mListingSelected;
    save(comment, false);
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
    Listing listing = mListingSelected;
    if (((Archivable) listing).isArchived()) {
      mMainView.showToast(R.string.listing_archived);
    } else if (!mAccessTokenManager.isUserAuthorized()) {
      mMainView.showToast(R.string.user_required);
    } else {
      mMainView.showToast(R.string.implementation_pending);
    }
  }

  @Override
  public void openCommentLink(@NonNull Comment comment) {
    // Link is already being displayed with this presenter
  }

  private void vote(int direction) {
    Listing listing = mListingSelected;
    if (((Archivable) listing).isArchived()) {
      mMainView.showToast(R.string.listing_archived);
    } else if (!mAccessTokenManager.isUserAuthorized()) {
      mMainView.showToast(R.string.user_required);
    } else {
      Votable votable = (Votable) listing;
      mRedditService.vote(votable, direction)
          .subscribe(response -> {
            votable.applyVote(direction);
            if (listing instanceof Link) {
              mLinkCommentsView.linkUpdated();
            } else {
              int index = mCommentBank.visibleIndexOf(((AbsComment) listing));
              mLinkCommentsView.commentUpdatedAt(index);
            }
          }, error -> mMainView.showToast(R.string.vote_failed));
      mAnalytics.logVote(votable.getKind(), direction);
    }
  }

  private void save(Savable savable, boolean toSave) {
    mRedditService.save(savable, null, toSave)
        .subscribe(response -> {
          savable.isSaved(toSave);
          if (savable instanceof Link) {
            mLinkCommentsView.linkUpdated();
          } else {
            mLinkCommentsView.commentUpdatedAt(
                mCommentBank.visibleIndexOf(((AbsComment) savable)));
          }
        }, error -> mMainView.showToast(R.string.save_failed));
  }
}
