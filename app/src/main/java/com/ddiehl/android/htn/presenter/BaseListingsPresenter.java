package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.android.dlogger.Logger;
import com.ddiehl.android.htn.AccessTokenManager;
import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.SettingsManager;
import com.ddiehl.android.htn.ThumbnailMode;
import com.ddiehl.android.htn.analytics.Analytics;
import com.ddiehl.android.htn.io.RedditService;
import com.ddiehl.android.htn.view.CommentView;
import com.ddiehl.android.htn.view.LinkView;
import com.ddiehl.android.htn.view.ListingsView;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.PrivateMessageView;
import com.ddiehl.android.htn.view.UserProfileView;
import com.ddiehl.reddit.Archivable;
import com.ddiehl.reddit.Hideable;
import com.ddiehl.reddit.Savable;
import com.ddiehl.reddit.Votable;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.CommentStub;
import com.ddiehl.reddit.listings.Link;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.ListingResponse;
import com.ddiehl.reddit.listings.ListingResponseData;
import com.ddiehl.reddit.listings.PrivateMessage;
import com.ddiehl.reddit.listings.Subreddit;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;

public abstract class BaseListingsPresenter
    implements ListingsPresenter, IdentityManager.Callbacks {
  protected Logger mLog = HoldTheNarwhal.getLogger();
  protected AccessTokenManager mAccessTokenManager = HoldTheNarwhal.getAccessTokenManager();
  protected IdentityManager mIdentityManager = HoldTheNarwhal.getIdentityManager();
  protected SettingsManager mSettingsManager = HoldTheNarwhal.getSettingsManager();
  protected RedditService mRedditService = HoldTheNarwhal.getRedditService();
  protected Analytics mAnalytics = HoldTheNarwhal.getAnalytics();

  protected List<Listing> mListings = new ArrayList<>();
  protected ListingsView mListingsView;
  protected MainView mMainView;
  protected LinkView mLinkView;
  protected CommentView mCommentView;
  protected UserProfileView mUserProfileView;
  protected PrivateMessageView mPrivateMessageView;

  protected String mShow;
  protected String mUsernameContext;
  protected String mSubreddit;
  protected String mSort;
  protected String mTimespan;
  protected Subreddit mSubredditInfo;

  protected Listing mListingSelected;
  protected boolean mListingsRequested = false;
  protected String mNextPageListingId;

  public BaseListingsPresenter(
      MainView main, ListingsView view, LinkView linkView, CommentView commentView,
      UserProfileView userProfileView, PrivateMessageView messageView,
      String show, String username, String subreddit, String sort, String timespan) {
    mListingsView = view;
    mLinkView = linkView;
    mCommentView = commentView;
    mUserProfileView = userProfileView;
    mPrivateMessageView = messageView;
    mMainView = main;
    mShow = show;
    mUsernameContext = username;
    mSubreddit = subreddit;
    mSort = sort;
    mTimespan = timespan;
  }

  @Override
  public void onResume() {
    mIdentityManager.registerUserIdentityChangeListener(this);
    if (!mListingsRequested && mListings.size() == 0) {
      refreshData();
    }
  }

  @Override
  public void onPause() {
    mIdentityManager.unregisterUserIdentityChangeListener(this);
  }

  @Override
  public void onViewDestroyed() {
    mListingSelected = null;
    mListings.clear();
    mListingsView.listingsUpdated();
  }

  @Override
  public void refreshData() {
    mListings.clear();
    mListingsView.listingsUpdated();
    mNextPageListingId = null;
    getMoreData();
  }

  @Override
  public void getMoreData() {
    if (!mListingsRequested) {
      requestData();
    }
  }

  abstract void requestData();

  @Override
  public void setData(@NonNull List<Listing> data) {
    mListings.clear();
    mListings.addAll(data);
  }

  @Override
  public void setSelectedListing(@NonNull Listing listing) {
    mListingSelected = listing;
  }

  @Override
  public Action1<UserIdentity> onUserIdentityChanged() {
    return identity -> refreshData();
  }

  @Override
  public int getNumListings() {
    return mListings.size();
  }

  @Override
  public Listing getListing(int position) {
    return mListings.get(position);
  }

  @Override
  public String getShow() {
    return mShow;
  }

  @Override
  public String getUsernameContext() {
    return mUsernameContext;
  }

  @Override
  public String getSubreddit() {
    return mSubreddit;
  }

  @Override
  public String getSort() {
    return mSort;
  }

  @Override
  public String getTimespan() {
    return mTimespan;
  }

  @Override
  public String getNextPageListingId() {
    return mNextPageListingId;
  }

  @Override
  public boolean getShowControversiality() {
    return mSettingsManager.getShowControversiality();
  }

  protected Action1<ListingResponse> onListingsLoaded() {
    return (response) -> {
      mMainView.dismissSpinner();
      mListingsRequested = false;
      if (response == null) {
        mMainView.showToast(R.string.error_xxx);
        return;
      }
      ListingResponseData data = response.getData();
      List<Listing> listings = data.getChildren();
      if (listings == null) {
        mMainView.showError(new NullPointerException(), R.string.error_get_links);
        return;
      }
      mListings.addAll(listings);
      mListingsView.listingsUpdated();
      mNextPageListingId = data.getAfter();
    };
  }

  public void showLinkContextMenu(
      ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Link link) {
    mListingSelected = link;
    mLinkView.showLinkContextMenu(menu, v, link);
    menu.findItem(R.id.action_link_reply).setVisible(false);
    menu.findItem(R.id.action_link_save).setVisible(!link.isSaved());
    menu.findItem(R.id.action_link_unsave).setVisible(link.isSaved());
  }

  public void openLink(@NonNull Link link) {
    if (link.isSelf()) {
      mLinkView.showCommentsForLink(link.getSubreddit(), link.getId(), null);
    } else {
      mLinkView.openLinkInWebView(link);
    }
  }

  public void showCommentsForLink() {
    Link link = (Link) mListingSelected;
    mLinkView.showCommentsForLink(link.getSubreddit(), link.getId(), null);
  }

  public void showCommentsForLink(@NonNull Link link) {
    mLinkView.showCommentsForLink(link.getSubreddit(), link.getId(), null);
  }

  public void replyToLink() {
    mCommentView.openReplyView(mListingSelected);
  }

  public void upvoteLink() {
    Votable votable = (Votable) mListingSelected;
    int dir = (votable.isLiked() == null || !votable.isLiked()) ? 1 : 0;
    vote(dir);
  }

  public void downvoteLink() {
    Votable votable = (Votable) mListingSelected;
    int dir = (votable.isLiked() == null || votable.isLiked()) ? -1 : 0;
    vote(dir);
  }

  public void saveLink() {
    if (!mAccessTokenManager.isUserAuthorized()) {
      mMainView.showToast(R.string.user_required);
      return;
    }
    Link link = (Link) mListingSelected;
    save(link, true);
    mAnalytics.logSave(link.getKind(), null, true);
  }

  public void unsaveLink() {
    if (!mAccessTokenManager.isUserAuthorized()) {
      mMainView.showToast(R.string.user_required);
      return;
    }
    Link link = (Link) mListingSelected;
    save(link, false);
    mAnalytics.logSave(link.getKind(), null, false);
  }

  public void shareLink() {
    Link link = (Link) mListingSelected;
    mLinkView.openShareView(link);
  }

  public void openLinkUserProfile() {
    Link link = (Link) mListingSelected;
    mLinkView.openUserProfileView(link);
  }

  public void openLinkUserProfile(@NonNull Link link) {
    mLinkView.openUserProfileView(link);
  }

  public void openLinkInBrowser() {
    Link link = (Link) mListingSelected;
    mLinkView.openLinkInBrowser(link);
  }

  public void openCommentsInBrowser() {
    Link link = (Link) mListingSelected;
    mLinkView.openCommentsInBrowser(link);
  }

  public void hideLink() {
    if (!mAccessTokenManager.isUserAuthorized()) {
      mMainView.showToast(R.string.user_required);
      return;
    }

    Link link = (Link) mListingSelected;
    hide(link, true);
    mAnalytics.logHide(link.getKind(), true);
  }

  public void unhideLink() {
    if (!mAccessTokenManager.isUserAuthorized()) {
      mMainView.showToast(R.string.user_required);
      return;
    }

    Link link = (Link) mListingSelected;
    hide(link, false);
    mAnalytics.logHide(link.getKind(), false);
  }

  public void reportLink() {
    Listing listing = mListingSelected;
    if (((Archivable) listing).isArchived()) {
      mMainView.showToast(R.string.listing_archived);
    } else if (!mAccessTokenManager.isUserAuthorized()) {
      mMainView.showToast(R.string.user_required);
    } else {
      mMainView.showToast(R.string.implementation_pending);
    }
  }

  public void showCommentThread(
      @NonNull String subreddit, @NonNull String linkId, @NonNull String commentId) {
    if (linkId.charAt(2) == '_') linkId = linkId.substring(3); // Trim type prefix
    mMainView.showCommentsForLink(subreddit, linkId, commentId);
  }

  public void getMoreComments(@NonNull CommentStub comment) {
    // Comment stubs cannot appear in a listing view
  }

  public void showCommentContextMenu(
      ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Comment comment) {
    mListingSelected = comment;
    mCommentView.showCommentContextMenu(menu, v, comment);
    menu.findItem(R.id.action_comment_save).setVisible(!comment.isSaved());
    menu.findItem(R.id.action_comment_unsave).setVisible(comment.isSaved());
  }

  public void openCommentPermalink() {
    Comment comment = (Comment) mListingSelected;
    showCommentThread(comment.getSubreddit(), comment.getLinkId(), comment.getId());
  }

  public void replyToComment() {
    Comment comment = (Comment) mListingSelected;
    if (comment.isArchived()) {
      mMainView.showToast(R.string.listing_archived);
    } else {
      mCommentView.openReplyView(comment);
    }
  }

  public void upvoteComment() {
    Votable votable = (Votable) mListingSelected;
    int dir = (votable.isLiked() == null || !votable.isLiked()) ? 1 : 0;
    vote(dir);
  }

  public void downvoteComment() {
    Votable votable = (Votable) mListingSelected;
    int dir = (votable.isLiked() == null || votable.isLiked()) ? -1 : 0;
    vote(dir);
  }

  public void saveComment() {
    if (!mAccessTokenManager.isUserAuthorized()) {
      mMainView.showToast(R.string.user_required);
      return;
    }
    Comment comment = (Comment) mListingSelected;
    save(comment, true);
    mAnalytics.logSave(comment.getKind(), null, true);
  }

  public void unsaveComment() {
    if (!mAccessTokenManager.isUserAuthorized()) {
      mMainView.showToast(R.string.user_required);
      return;
    }
    Comment comment = (Comment) mListingSelected;
    save(comment, false);
    mAnalytics.logSave(comment.getKind(), null, false);
  }

  public void shareComment() {
    Comment comment = (Comment) mListingSelected;
    mCommentView.openShareView(comment);
  }

  public void openCommentUserProfile() {
    Comment comment = (Comment) mListingSelected;
    mCommentView.openUserProfileView(comment);
  }

  public void openCommentUserProfile(@NonNull Comment comment) {
    mCommentView.openUserProfileView(comment);
  }

  public void openCommentInBrowser() {
    Comment comment = (Comment) mListingSelected;
    mCommentView.openCommentInBrowser(comment);
  }

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

  public void openCommentLink(@NonNull Comment comment) {
    mMainView.showCommentsForLink(comment.getSubreddit(), comment.getLinkId(), null);
  }

  public void showMessageContextMenu(
      ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, PrivateMessage message) {
    mPrivateMessageView.showPrivateMessageContextMenu(menu, v, message);
  }

  @Override
  public UserIdentity getAuthorizedUser() {
    return mIdentityManager.getUserIdentity();
  }

  @Override
  public void onSortSelected(@Nullable String sort) {
    if (sort == null) return; // Nothing happened
    boolean changed = false;
    if (!mSort.equals(sort)) {
      mAnalytics.logOptionChangeSort(sort);
      mSort = sort;
      changed = true;
    }
    if (sort.equals("top") || sort.equals("controversial")) {
      mListingsView.showTimespanOptionsMenu();
    } else if (changed) {
      mListingsView.onSortChanged();
      onSortChanged();
    }
  }

  @Override
  public void onSortChanged() {
    if ((mSort.equals("top") || mSort.equals("controversial"))
        && mTimespan == null){
      mTimespan = "all";
    }
    refreshData();
  }

  @Override
  public void onTimespanSelected(@Nullable String timespan) {
    if (!TextUtils.equals(mTimespan, timespan) && timespan != null) {
      mTimespan = timespan;
      mAnalytics.logOptionChangeTimespan(timespan);
      mListingsView.onTimespanChanged();
    }
    onSortChanged(); // Sort was still changed to bring up this prompt, fire the callback
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
            mListingsView.listingUpdatedAt(
                mListings.indexOf(listing));
          }, e -> mMainView.showError(e, R.string.vote_failed));
      mAnalytics.logVote(votable.getKind(), direction);
    }
  }

  private void save(Savable savable, boolean toSave) {
    Listing listing = mListingSelected;
    mRedditService.save(savable, null, toSave)
        .subscribe(response -> {
          savable.isSaved(toSave);
          mListingsView.listingUpdatedAt(
              mListings.indexOf(listing));
        }, e -> mMainView.showError(e, R.string.save_failed));
  }

  private void hide(Hideable hideable, boolean toHide) {
    Listing listing = mListingSelected;
    mRedditService.hide(hideable, toHide)
        .subscribe(response -> {
          int pos = mListings.indexOf(listing);
          if (toHide) {
            mMainView.showToast(R.string.link_hidden);
            mListings.remove(pos);
            mListingsView.listingRemovedAt(pos);
          } else {
            mListingsView.listingRemovedAt(pos);
          }
        }, e -> mMainView.showError(e, R.string.hide_failed));
  }

  @Override
  public void onNsfwSelected(boolean nsfwAllowed) {
    if (nsfwAllowed) {
      mSettingsManager.setOver18(true);
      refreshData();
    } else {
      mMainView.dismissSpinner();
      mMainView.goBack();
    }
  }

  public boolean shouldShowNsfwTag() {
    return !mSettingsManager.getOver18() || !(mSubredditInfo != null && mSubredditInfo.isOver18())
        && (mSettingsManager.getNoProfanity() || mSettingsManager.getLabelNsfw());
  }

  public ThumbnailMode getThumbnailMode() {
    if (mSettingsManager.getOver18()) {
      if (mSubredditInfo != null && mSubredditInfo.isOver18()) {
        return ThumbnailMode.FULL;
      } else {
        if (mSettingsManager.getNoProfanity()) {
          return ThumbnailMode.VARIANT;
        } else {
          if (mSettingsManager.getLabelNsfw()) {
            return ThumbnailMode.VARIANT;
          } else {
            return ThumbnailMode.FULL;
          }
        }
      }
    } else {
      return ThumbnailMode.NO_THUMBNAIL;
    }
  }

  public UserIdentity getUserIdentity() {
    return mIdentityManager.getUserIdentity();
  }

  public void replyToMessage() {
    PrivateMessage message = (PrivateMessage) mListingSelected;
    mMainView.showToast(R.string.implementation_pending);
  }

  public void markMessageUnread() {
    PrivateMessage message = (PrivateMessage) mListingSelected;
    String fullname = message.getFullName();
    mRedditService.markMessagesUnread(fullname)
        .subscribe(
            _void -> {
              message.markUnread(true);
              mListingsView.listingUpdatedAt(
                  mListings.indexOf(message));
            },
            error -> mMainView.showError(error, R.string.error_xxx)
        );
  }

  public void showMessagePermalink() {
    PrivateMessage message = (PrivateMessage) mListingSelected;
    ListingResponse listingResponse = message.getReplies();
    List<PrivateMessage> messages = new ArrayList<>();
    if (listingResponse != null) {
      for (Listing item : listingResponse.getData().getChildren()) {
        messages.add((PrivateMessage) item);
      }
    }
    messages.add(0, message);
    mMainView.showInboxMessages(messages);
  }

  public void reportMessage() {
    PrivateMessage message = (PrivateMessage) mListingSelected;
    mMainView.showToast(R.string.implementation_pending);
  }

  public void blockUser() {
    PrivateMessage message = (PrivateMessage) mListingSelected;
    mMainView.showToast(R.string.implementation_pending);
  }
}
