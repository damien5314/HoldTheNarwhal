package com.ddiehl.android.htn.presenter;

import android.content.Context;
import android.support.annotation.NonNull;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.SettingsManager;
import com.ddiehl.android.htn.ThumbnailMode;
import com.ddiehl.android.htn.analytics.Analytics;
import com.ddiehl.android.htn.utils.AndroidUtils;
import com.ddiehl.android.htn.view.CommentView;
import com.ddiehl.android.htn.view.LinkView;
import com.ddiehl.android.htn.view.ListingsView;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.PrivateMessageView;
import com.ddiehl.android.htn.view.RedditNavigationView;

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
        implements ListingsPresenter, ListingsView.Callbacks {

    @Inject protected Context mContext;
    @Inject protected IdentityManager mIdentityManager;
    @Inject protected SettingsManager mSettingsManager;
    @Inject protected RedditService mRedditService;
    @Inject protected Analytics mAnalytics;

    protected final List<Listing> mListings = new ArrayList<>();

    private final ListingsView mListingsView;
    protected final MainView mMainView;
    protected final RedditNavigationView mRedditNavigationView;
    private final LinkView mLinkView;
    private final CommentView mCommentView;
    private final PrivateMessageView mPrivateMessageView;

    protected Subreddit mSubredditInfo;

    protected boolean mBeforeRequested, mNextRequested = false;
    protected String mPrevPageListingId, mNextPageListingId;

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

    @Override
    public void onResume() {
        // FIXME Do we need to check mNextRequested here?
        if (!mNextRequested && mListings.size() == 0) {
            refreshData();
        }
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onViewDestroyed() {
        // To disable the memory dereferencing functionality just comment these lines
        mListings.clear();
        mListingsView.notifyDataSetChanged();
    }

    @Override
    public void refreshData() {
        mPrevPageListingId = null;
        mNextPageListingId = null;
        int numItems = mListings.size();
        mListings.clear();
        mListingsView.notifyItemRangeRemoved(0, numItems);
        getNextData();
    }

    @Override
    public void getPreviousData() {
        if (!mBeforeRequested) {
            if (AndroidUtils.isConnectedToNetwork(mContext)) {
                requestPreviousData();
            } else {
                mMainView.showToast(mContext.getString(R.string.error_network_unavailable));
            }
        }
    }

    @Override
    public void getNextData() {
        if (!mNextRequested) {
            if (AndroidUtils.isConnectedToNetwork(mContext)) {
                requestNextData();
            } else {
                mMainView.showToast(mContext.getString(R.string.error_network_unavailable));
            }
        }
    }

    abstract void requestPreviousData();

    abstract void requestNextData();

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

    @Override
    public void setData(@NonNull List<Listing> data) {
        mListings.clear();
        mListings.addAll(data);
    }

    @Override
    public int getNumListings() {
        return mListings.size();
    }

    @Override
    public Listing getListingAt(int position) {
        return mListings.get(position);
    }

    @Override
    public boolean hasPreviousListings() {
        return mPrevPageListingId != null;
    }

    @Override
    public boolean hasNextListings() {
        return mNextPageListingId != null;
    }

    @Override
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
                mMainView.showError(new NullPointerException(), message);
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
        if (link.isArchived()) {
            mMainView.showToast(mContext.getString(R.string.listing_archived));
        } else if (!mRedditService.isUserAuthorized()) {
            mMainView.showToast(mContext.getString(R.string.user_required));
        } else {
            mMainView.showToast(mContext.getString(R.string.implementation_pending));
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
        vote(null, dir);
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
            mMainView.showToast(mContext.getString(R.string.implementation_pending));
        }
    }

    public void openCommentLink(@NonNull Comment comment) {
        mRedditNavigationView.showCommentsForLink(comment.getSubreddit(), comment.getLinkId(), null);
    }

    @Override
    public UserIdentity getAuthorizedUser() {
        return mIdentityManager.getUserIdentity();
    }

    @Override
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
                    .subscribe(response -> {
                        votable.applyVote(direction);
                        mListingsView.notifyItemChanged(getIndexOf((Listing) votable));
                    }, e -> {
                        String message = mContext.getString(R.string.vote_failed);
                        mMainView.showError(e, message);
                    });
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
                .subscribe(response -> {
                    savable.isSaved(toSave);
                    mListingsView.notifyItemChanged(getIndexOf((Listing) savable));
                }, e -> {
                    String message = mContext.getString(R.string.save_failed);
                    mMainView.showError(e, message);
                });
    }

    private void hide(Hideable hideable, boolean toHide) {
        mRedditService.hide(hideable.getFullName(), toHide)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    int pos = getIndexOf((Listing) hideable);
                    mListings.remove(pos);
                    mListingsView.notifyItemRemoved(pos);
                }, e -> {
                    String message = mContext.getString(R.string.hide_failed);
                    mMainView.showError(e, message);
                });
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

    public void markMessageRead(@NonNull PrivateMessage message) {
        String fullname = message.getFullName();
        mRedditService.markMessagesRead(fullname)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        _void -> {
                            message.markUnread(false);
                            mListingsView.notifyItemChanged(getIndexOf(message));
                        },
                        error -> {
                            String errorMessage = mContext.getString(R.string.error_xxx);
                            mMainView.showError(error, errorMessage);
                        }
                );
    }

    public void markMessageUnread(@NonNull PrivateMessage message) {
        String fullname = message.getFullName();
        mRedditService.markMessagesUnread(fullname)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        _void -> {
                            message.markUnread(true);
                            mListingsView.notifyItemChanged(getIndexOf(message));
                        },
                        error -> {
                            String errorMessage = mContext.getString(R.string.error_xxx);
                            mMainView.showError(error, errorMessage);
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
        mMainView.showToast(mContext.getString(R.string.implementation_pending));
    }

    public void blockUser(@NonNull PrivateMessage message) {
        mMainView.showToast(mContext.getString(R.string.implementation_pending));
    }
}
