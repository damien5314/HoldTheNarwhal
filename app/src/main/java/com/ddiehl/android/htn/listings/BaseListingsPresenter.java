package com.ddiehl.android.htn.listings;

import android.content.Context;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.identity.IdentityManager;
import com.ddiehl.android.htn.listings.comments.CommentView;
import com.ddiehl.android.htn.listings.inbox.PrivateMessageView;
import com.ddiehl.android.htn.listings.links.LinkView;
import com.ddiehl.android.htn.listings.subreddit.ThumbnailMode;
import com.ddiehl.android.htn.navigation.RedditNavigationView;
import com.ddiehl.android.htn.settings.SettingsManager;
import com.ddiehl.android.htn.utils.AndroidUtils;
import com.ddiehl.android.htn.view.MainView;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import rxreddit.api.RedditService;
import rxreddit.model.Comment;
import rxreddit.model.CommentStub;
import rxreddit.model.Hideable;
import rxreddit.model.Link;
import rxreddit.model.Listing;
import rxreddit.model.ListingResponse;
import rxreddit.model.ListingResponseData;
import rxreddit.model.Media;
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

    public void setData(@NotNull List<Listing> data) {
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

    protected void onListingsLoaded(ListingResponse response, boolean append) {
        mMainView.dismissSpinner();

        if (append) mNextRequested = false;
        else mBeforeRequested = false;

        if (response == null) {
            mMainView.showToast(mContext.getString(R.string.error_xxx));
            return;
        }

        ListingResponseData data = response.getData();
        List<Listing> listings = data.getChildren();

        Timber.i("Loaded %d listings", listings.size());

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

    protected ObservableSource<ListingResponse> checkNullResponse(ListingResponse listingResponse) {
        if (listingResponse.getData().getChildren() == null) {
            mPrevPageListingId = null;
            mNextPageListingId = null;
            return Observable.error(new NullPointerException("no links"));
        } else {
            return Observable.just(listingResponse);
        }
    }

    public void openLink(@NotNull Link link) {
        if (link.isSelf()) {
            mLinkView.showCommentsForLink(link.getSubreddit(), link.getId(), null);
        } else {
            final Media media = link.getMedia();
            if (media != null) {
                final Media.RedditVideo redditVideo = media.getRedditVideo();
                if (redditVideo != null) {
                    mLinkView.openUrlInWebView(redditVideo.getFallbackUrl());
                    return;
                }
            }
            mLinkView.openUrlInWebView(link.getUrl());
        }
    }

    public void showCommentsForLink(@NotNull Link link) {
        mLinkView.showCommentsForLink(link.getSubreddit(), link.getId(), null);
    }

    public void replyToLink(Link link) {
        mCommentView.openReplyView(link);
    }

    public void upvoteLink(@NotNull Link link) {
        int dir = (link.isLiked() == null || !link.isLiked()) ? 1 : 0;
        vote(link, dir);
    }

    public void downvoteLink(@NotNull Link link) {
        int dir = (link.isLiked() == null || link.isLiked()) ? -1 : 0;
        vote(link, dir);
    }

    public void saveLink(@NotNull Link link) {
        if (!mRedditService.isUserAuthorized()) {
            mMainView.showToast(mContext.getString(R.string.user_required));
            return;
        }
        save(link, true);
    }

    public void unsaveLink(@NotNull Link link) {
        if (!mRedditService.isUserAuthorized()) {
            mMainView.showToast(mContext.getString(R.string.user_required));
            return;
        }
        save(link, false);
    }

    public void shareLink(@NotNull Link link) {
        mLinkView.openShareView(link);
    }

    public void openLinkSubreddit(@NotNull Link link) {
        String subreddit = link.getSubreddit();
        mLinkView.openSubredditView(subreddit);
    }

    public void openLinkUserProfile(@NotNull Link link) {
        mLinkView.openUserProfileView(link);
    }

    public void openLinkInBrowser(@NotNull Link link) {
        mLinkView.openLinkInBrowser(link);
    }

    public void openCommentsInBrowser(@NotNull Link link) {
        mLinkView.openCommentsInBrowser(link);
    }

    public void hideLink(@NotNull Link link) {
        if (!mRedditService.isUserAuthorized()) {
            mMainView.showToast(mContext.getString(R.string.user_required));
            return;
        }

        hide(link, true);
    }

    public void unhideLink(@NotNull Link link) {
        if (!mRedditService.isUserAuthorized()) {
            mMainView.showToast(mContext.getString(R.string.user_required));
            return;
        }

        hide(link, false);
    }

    public void reportLink(@NotNull Link link) {
        if (!mRedditService.isUserAuthorized()) {
            mMainView.showToast(mContext.getString(R.string.user_required));
        } else {
            mLinkView.openReportView(link);
        }
    }

    public void showCommentThread(
            @NotNull String subreddit, @NotNull String linkId, @NotNull String commentId) {
        mRedditNavigationView.showCommentsForLink(subreddit, linkId, commentId);
    }

    public void getMoreComments(@NotNull CommentStub comment) {
        // Comment stubs cannot appear in a listing view
    }

    public void openCommentPermalink(@NotNull Comment comment) {
        showCommentThread(comment.getSubreddit(), comment.getLinkId(), comment.getId());
    }

    public void openCommentParent(@NotNull Comment comment) {
        showCommentThread(comment.getSubreddit(), comment.getLinkId(), comment.getParentId());
    }

    public void replyToComment(@NotNull Comment comment) {
        if (comment.isArchived()) {
            mMainView.showToast(mContext.getString(R.string.listing_archived));
        } else {
            mCommentView.openReplyView(comment);
        }
    }

    public void upvoteComment(@NotNull Comment comment) {
        int dir = (comment.isLiked() == null || !comment.isLiked()) ? 1 : 0;
        vote(comment, dir);
    }

    public void downvoteComment(@NotNull Comment comment) {
        int dir = (comment.isLiked() == null || comment.isLiked()) ? -1 : 0;
        vote(comment, dir);
    }

    public void saveComment(@NotNull Comment comment) {
        if (!mRedditService.isUserAuthorized()) {
            mMainView.showToast(mContext.getString(R.string.user_required));
            return;
        }
        save(comment, true);
    }

    public void unsaveComment(@NotNull Comment comment) {
        if (!mRedditService.isUserAuthorized()) {
            mMainView.showToast(mContext.getString(R.string.user_required));
            return;
        }
        save(comment, false);
    }

    public void shareComment(@NotNull Comment comment) {
        mCommentView.openShareView(comment);
    }

    public void openCommentUserProfile(@NotNull Comment comment) {
        mCommentView.openUserProfileView(comment);
    }

    public void openCommentInBrowser(@NotNull Comment comment) {
        mCommentView.openCommentInBrowser(comment);
    }

    public void reportComment(@NotNull Comment comment) {
        if (comment.isArchived()) {
            mMainView.showToast(mContext.getString(R.string.listing_archived));
        } else if (!mRedditService.isUserAuthorized()) {
            mMainView.showToast(mContext.getString(R.string.user_required));
        } else {
            mCommentView.openReportView(comment);
        }
    }

    public void openCommentLink(@NotNull Comment comment) {
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
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            () -> {
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
                        () -> {
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
                        () -> {
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

    public void replyToMessage(@NotNull PrivateMessage message) {
        mMainView.showToast(mContext.getString(R.string.implementation_pending));
    }

    public void markMessageRead(@NotNull PrivateMessage pm) {
        String fullname = pm.getFullName();
        mRedditService.markMessagesRead(fullname)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
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

    public void markMessageUnread(@NotNull PrivateMessage pm) {
        String fullname = pm.getFullName();
        mRedditService.markMessagesUnread(fullname)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
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

    public void showMessagePermalink(@NotNull PrivateMessage message) {
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

    public void reportMessage(@NotNull PrivateMessage message) {
        mPrivateMessageView.openReportView(message);
    }

    public void blockUser(@NotNull PrivateMessage message) {
        mMainView.showToast(mContext.getString(R.string.implementation_pending));
    }
}
