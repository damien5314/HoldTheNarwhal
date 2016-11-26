package com.ddiehl.android.htn.listings;

import android.content.Context;
import android.support.annotation.NonNull;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.analytics.Analytics;
import com.ddiehl.android.htn.identity.IdentityManager;
import com.ddiehl.android.htn.listings.comments.CommentView;
import com.ddiehl.android.htn.listings.inbox.PrivateMessageView;
import com.ddiehl.android.htn.listings.links.LinkView;
import com.ddiehl.android.htn.listings.subreddit.ThumbnailMode;
import com.ddiehl.android.htn.navigation.RedditNavigationView;
import com.ddiehl.android.htn.settings.SettingsManager;
import com.ddiehl.android.htn.utils.AndroidUtils;
import com.ddiehl.android.htn.view.MainView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rxreddit.api.RedditService;
import rxreddit.model.Comment;
import rxreddit.model.CommentStub;
import rxreddit.model.Hideable;
import rxreddit.model.Link;
import rxreddit.model.Listing;
import rxreddit.model.ListingResponse;
import rxreddit.model.ListingResponseData;
import rxreddit.model.PrivateMessage;
import rxreddit.model.Savable;
import rxreddit.model.Subreddit;
import rxreddit.model.UserIdentity;
import rxreddit.model.Votable;
import timber.log.Timber;

public abstract class BaseListingsPresenter
        implements ListingsView.Callbacks {

    @Inject protected Context mContext;
    @Inject protected IdentityManager mIdentityManager;
    @Inject protected SettingsManager mSettingsManager;
    @Inject protected RedditService mRedditService;
    @Inject protected Analytics mAnalytics;

    final List<Listing> mListings = new ArrayList<>();

    private final ListingsView mListingsView;
    protected final MainView mMainView;
    protected final RedditNavigationView mRedditNavigationView;
    private final LinkView mLinkView;
    private final CommentView mCommentView;
    private final PrivateMessageView mPrivateMessageView;

    protected boolean mBeforeRequested, mNextRequested = false;
    protected String mPrevPageListingId, mNextPageListingId;

    protected Subreddit mSubredditInfo;

    public BaseListingsPresenter(
            MainView main, RedditNavigationView redditNavigationView,
            ListingsView view, LinkView linkView, CommentView commentView,
            PrivateMessageView messageView) {
        HoldTheNarwhal.getApplicationComponent().inject(this);
        mMainView = main;
        mRedditNavigationView = redditNavigationView;
        mListingsView = view;
        mLinkView = linkView;
        mCommentView = commentView;
        mPrivateMessageView = messageView;
    }

    public List<Listing> getListings() {
        return mListings;
    }

    public boolean hasData() {
        return mListings.size() != 0;
    }

    public void clearData() {
        mListings.clear();
        mListingsView.notifyDataSetChanged();
    }

    public void refreshData() {
        mPrevPageListingId = null;
        mNextPageListingId = null;
        int numItems = mListings.size();
        mListings.clear();
        mListingsView.notifyItemRangeRemoved(0, numItems);
        getNextData();
    }

    public void getPreviousData() {
        if (!mBeforeRequested) {
            if (AndroidUtils.isConnectedToNetwork(mContext)) {
                requestPreviousData();
            } else {
                String message = mContext.getString(R.string.error_network_unavailable);
                mMainView.showToast(message);
            }
        }
    }

    public void getNextData() {
        if (!mNextRequested) {
            if (AndroidUtils.isConnectedToNetwork(mContext)) {
                requestNextData();
            } else {
                String message = mContext.getString(R.string.error_network_unavailable);
                mMainView.showToast(message);
            }
        }
    }

    protected abstract void requestPreviousData();

    protected abstract void requestNextData();

    @Override
    public void onFirstItemShown() {
        if (!mBeforeRequested && hasPreviousListings()) {
            Timber.d("Get PREVIOUS data");
            getPreviousData();
        }
    }

    @Override
    public void onLastItemShown() {
        if (!mNextRequested && hasNextListings()) {
            Timber.d("Get NEXT data");
            getNextData();
        }
    }

    public void setData(@NonNull List<Listing> data) {
        mListings.clear();
        mListings.addAll(data);
    }

    public int getNumListings() {
        return mListings.size();
    }

    public Listing getListingAt(int position) {
        return mListings.get(position);
    }

    public boolean hasPreviousListings() {
        return mPrevPageListingId != null;
    }

    public boolean hasNextListings() {
        return mNextPageListingId != null;
    }

    public boolean getShowControversiality() {
        return mSettingsManager.getShowControversiality();
    }

    protected Action1<ListingResponse> onListingsLoaded(boolean append) {
        return (response) -> {
            mMainView.dismissSpinner();

            if (append) mNextRequested = false;
            else mBeforeRequested = false;

            if (response == null) {
                mMainView.showToast(mContext.getString(R.string.error_xxx));
                return;
            }

            ListingResponseData data = response.getData();
            List<Listing> listings = data.getChildren();

            if (listings == null) {
                mPrevPageListingId = null;
                mNextPageListingId = null;

                String message = mContext.getString(R.string.error_get_links);
                mMainView.showError(message);
            } else {
                if (append) {
                    int lastIndex = mListings.size() - 1;
                    mListings.addAll(listings);
                    mNextPageListingId = data.getAfter();
                    mListingsView.notifyItemRangeInserted(lastIndex + 1, listings.size());
                } else {
                    mListings.addAll(0, listings);
                    mPrevPageListingId = listings.size() == 0 ? null : listings.get(0).getFullName();
                    mListingsView.notifyItemRangeInserted(0, listings.size());
                }
            }
        };
    }

    public void openLink(@NonNull Link link) {
        if (link.isSelf()) {
            mLinkView.showCommentsForLink(link.getSubreddit(), link.getId(), null);
        } else {
            mLinkView.openLinkInWebView(link);
        }
    }

    public void showCommentsForLink(@NonNull Link link) {
        mLinkView.showCommentsForLink(link.getSubreddit(), link.getId(), null);
    }

    public void replyToLink(Link link) {
        mCommentView.openReplyView(link);
    }

    public void upvoteLink(@NonNull Link link) {
        int dir = (link.isLiked() == null || !link.isLiked()) ? 1 : 0;
        vote(link, dir);
    }

    public void downvoteLink(@NonNull Link link) {
        int dir = (link.isLiked() == null || link.isLiked()) ? -1 : 0;
        vote(link, dir);
    }

    public void saveLink(@NonNull Link link) {
        if (!mRedditService.isUserAuthorized()) {
            mMainView.showToast(mContext.getString(R.string.user_required));
            return;
        }
        save(link, true);
        mAnalytics.logSave(link.getKind(), null, true);
    }

    public void unsaveLink(@NonNull Link link) {
        if (!mRedditService.isUserAuthorized()) {
            mMainView.showToast(mContext.getString(R.string.user_required));
            return;
        }
        save(link, false);
        mAnalytics.logSave(link.getKind(), null, false);
    }

    public void shareLink(@NonNull Link link) {
        mLinkView.openShareView(link);
    }

    public void openLinkSubreddit(@NonNull Link link) {
        String subreddit = link.getSubreddit();
        mLinkView.openSubredditView(subreddit);
    }

    public void openLinkUserProfile(@NonNull Link link) {
        mLinkView.openUserProfileView(link);
    }

    public void openLinkInBrowser(@NonNull Link link) {
        mLinkView.openLinkInBrowser(link);
    }

    public void openCommentsInBrowser(@NonNull Link link) {
        mLinkView.openCommentsInBrowser(link);
    }

    public void hideLink(@NonNull Link link) {
        if (!mRedditService.isUserAuthorized()) {
            mMainView.showToast(mContext.getString(R.string.user_required));
            return;
        }

        hide(link, true);
        mAnalytics.logHide(link.getKind(), true);
    }

    public void unhideLink(@NonNull Link link) {
        if (!mRedditService.isUserAuthorized()) {
            mMainView.showToast(mContext.getString(R.string.user_required));
            return;
        }

        hide(link, false);
        mAnalytics.logHide(link.getKind(), false);
    }

    public void reportLink(@NonNull Link link) {
        if (!mRedditService.isUserAuthorized()) {
            mMainView.showToast(mContext.getString(R.string.user_required));
        } else {
            mLinkView.openReportView(link);
        }
    }

    public void showCommentThread(
            @NonNull String subreddit, @NonNull String linkId, @NonNull String commentId) {
        mRedditNavigationView.showCommentsForLink(subreddit, linkId, commentId);
    }

    public void getMoreComments(@NonNull CommentStub comment) {
        // Comment stubs cannot appear in a listing view
    }

    public void openCommentPermalink(@NonNull Comment comment) {
        showCommentThread(comment.getSubreddit(), comment.getLinkId(), comment.getId());
    }

    public void replyToComment(@NonNull Comment comment) {
        if (comment.isArchived()) {
            mMainView.showToast(mContext.getString(R.string.listing_archived));
        } else {
            mCommentView.openReplyView(comment);
        }
    }

    public void upvoteComment(@NonNull Comment comment) {
        int dir = (comment.isLiked() == null || !comment.isLiked()) ? 1 : 0;
        vote(comment, dir);
    }

    public void downvoteComment(@NonNull Comment comment) {
        int dir = (comment.isLiked() == null || comment.isLiked()) ? -1 : 0;
        vote(comment, dir);
    }

    public void saveComment(@NonNull Comment comment) {
        if (!mRedditService.isUserAuthorized()) {
            mMainView.showToast(mContext.getString(R.string.user_required));
            return;
        }
        save(comment, true);
        mAnalytics.logSave(comment.getKind(), null, true);
    }

    public void unsaveComment(@NonNull Comment comment) {
        if (!mRedditService.isUserAuthorized()) {
            mMainView.showToast(mContext.getString(R.string.user_required));
            return;
        }
        save(comment, false);
        mAnalytics.logSave(comment.getKind(), null, false);
    }

    public void shareComment(@NonNull Comment comment) {
        mCommentView.openShareView(comment);
    }

    public void openCommentUserProfile(@NonNull Comment comment) {
        mCommentView.openUserProfileView(comment);
    }

    public void openCommentInBrowser(@NonNull Comment comment) {
        mCommentView.openCommentInBrowser(comment);
    }

    public void reportComment(@NonNull Comment comment) {
        if (comment.isArchived()) {
            mMainView.showToast(mContext.getString(R.string.listing_archived));
        } else if (!mRedditService.isUserAuthorized()) {
            mMainView.showToast(mContext.getString(R.string.user_required));
        } else {
            mCommentView.openReportView(comment);
        }
    }

    public void openCommentLink(@NonNull Comment comment) {
        mRedditNavigationView.showCommentsForLink(comment.getSubreddit(), comment.getLinkId(), null);
    }

    public UserIdentity getAuthorizedUser() {
        return mIdentityManager.getUserIdentity();
    }

    public void onSortChanged() {
        refreshData();
    }

    private void vote(Votable votable, int direction) {
        if (votable.isArchived()) {
            mMainView.showToast(mContext.getString(R.string.listing_archived));
        } else if (!mRedditService.isUserAuthorized()) {
            mMainView.showToast(mContext.getString(R.string.user_required));
        } else {
            mRedditService.vote(votable.getKind() + "_" + votable.getId(), direction)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            response -> {
                                votable.applyVote(direction);
                                mListingsView.notifyItemChanged(getIndexOf((Listing) votable));
                            },
                            error -> {
                                if (error instanceof IOException) {
                                    String message = mContext.getString(R.string.error_network_unavailable);
                                    mMainView.showError(message);
                                } else {
                                    Timber.w(error, "Error voting on listing");
                                    String message = mContext.getString(R.string.vote_failed);
                                    mMainView.showError(message);
                                }
                            }
                    );
            mAnalytics.logVote(votable.getKind(), direction);
        }
    }

    /**
     * This is overridden in link comments view which has headers
     */
    protected int getIndexOf(Listing listing) {
        return mListings.indexOf(listing);
    }

    private void save(Savable savable, boolean toSave) {
        mRedditService.save(savable.getFullName(), null, toSave)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            savable.isSaved(toSave);
                            mListingsView.notifyItemChanged(getIndexOf((Listing) savable));
                        },
                        error -> {
                            if (error instanceof IOException) {
                                String message = mContext.getString(R.string.error_network_unavailable);
                                mMainView.showError(message);
                            } else {
                                Timber.w(error, "Error saving listing");
                                String message = mContext.getString(R.string.save_failed);
                                mMainView.showError(message);
                            }
                        }
                );
    }

    private void hide(Hideable hideable, boolean toHide) {
        mRedditService.hide(hideable.getFullName(), toHide)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            int pos = getIndexOf((Listing) hideable);
                            mListings.remove(pos);
                            mListingsView.notifyItemRemoved(pos);
                        },
                        error -> {
                            if (error instanceof IOException) {
                                String message = mContext.getString(R.string.error_network_unavailable);
                                mMainView.showError(message);
                            } else {
                                Timber.w(error, "Error hiding listing");
                                String message = mContext.getString(R.string.hide_failed);
                                mMainView.showError(message);
                            }
                        }
                );
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

    public void replyToMessage(@NonNull PrivateMessage message) {
        mMainView.showToast(mContext.getString(R.string.implementation_pending));
    }

    public void markMessageRead(@NonNull PrivateMessage pm) {
        String fullname = pm.getFullName();
        mRedditService.markMessagesRead(fullname)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            pm.markUnread(false);
                            mListingsView.notifyItemChanged(getIndexOf(pm));
                        },
                        error -> {
                            if (error instanceof IOException) {
                                String message = mContext.getString(R.string.error_network_unavailable);
                                mMainView.showError(message);
                            } else {
                                Timber.w(error, "Error marking message read");
                                String errorMessage = mContext.getString(R.string.error_xxx);
                                mMainView.showError(errorMessage);
                            }
                        }
                );
    }

    public void markMessageUnread(@NonNull PrivateMessage pm) {
        String fullname = pm.getFullName();
        mRedditService.markMessagesUnread(fullname)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            pm.markUnread(true);
                            mListingsView.notifyItemChanged(getIndexOf(pm));
                        },
                        error -> {
                            if (error instanceof IOException) {
                                String message = mContext.getString(R.string.error_network_unavailable);
                                mMainView.showError(message);
                            } else {
                                Timber.w(error, "Error marking message unread");
                                String errorMessage = mContext.getString(R.string.error_xxx);
                                mMainView.showError(errorMessage);
                            }
                        }
                );
    }

    public void showMessagePermalink(@NonNull PrivateMessage message) {
        ListingResponse listingResponse = message.getReplies();
        List<PrivateMessage> messages = new ArrayList<>();
        if (listingResponse != null) {
            for (Listing item : listingResponse.getData().getChildren()) {
                messages.add((PrivateMessage) item);
            }
        }
        messages.add(0, message);
        mRedditNavigationView.showInboxMessages(messages);
    }

    public void reportMessage(@NonNull PrivateMessage message) {
        mPrivateMessageView.openReportView(message);
    }

    public void blockUser(@NonNull PrivateMessage message) {
        mMainView.showToast(mContext.getString(R.string.implementation_pending));
    }
}
