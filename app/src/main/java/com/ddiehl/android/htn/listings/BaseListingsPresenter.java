package com.ddiehl.android.htn.listings;

import android.content.Context;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.gallery.MediaGalleryRouter;
import com.ddiehl.android.htn.identity.IdentityManager;
import com.ddiehl.android.htn.listings.comments.CommentView;
import com.ddiehl.android.htn.listings.comments.LinkCommentsRouter;
import com.ddiehl.android.htn.listings.inbox.PrivateMessageView;
import com.ddiehl.android.htn.listings.links.LinkView;
import com.ddiehl.android.htn.listings.subreddit.ThumbnailMode;
import com.ddiehl.android.htn.managers.NetworkConnectivityManager;
import com.ddiehl.android.htn.routing.AppRouter;
import com.ddiehl.android.htn.settings.SettingsManager;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.video.VideoPlayerRouter;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.schedulers.Schedulers;
import rxreddit.api.RedditService;
import rxreddit.model.Comment;
import rxreddit.model.CommentStub;
import rxreddit.model.GalleryItem;
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

    @Inject
    protected Context context;
    @Inject
    protected IdentityManager identityManager;
    @Inject
    protected SettingsManager settingsManager;
    @Inject
    protected RedditService redditService;
    @Inject
    protected NetworkConnectivityManager networkConnectivityManager;

    final List<Listing> listings = new ArrayList<>();

    private final ListingsView listingsView;
    protected final MainView mainView;
    private final AppRouter appRouter;
    private final LinkCommentsRouter linkCommentsRouter;
    private final MediaGalleryRouter mediaGalleryRouter;
    private final VideoPlayerRouter videoPlayerRouter;
    private final LinkView linkView;
    private final CommentView commentView;
    private final PrivateMessageView privateMessageView;

    protected boolean beforeRequested, nextRequested = false;
    protected String prevPageListingId, nextPageListingId;

    protected Subreddit subreddit;

    public BaseListingsPresenter(
            MainView main,
            AppRouter appRouter,
            LinkCommentsRouter linkCommentsRouter,
            MediaGalleryRouter mediaGalleryRouter,
            VideoPlayerRouter videoPlayerRouter,
            ListingsView view,
            LinkView linkView,
            CommentView commentView,
            PrivateMessageView messageView) {
        HoldTheNarwhal.getApplicationComponent().inject(this);
        this.mainView = main;
        this.appRouter = appRouter;
        this.linkCommentsRouter = linkCommentsRouter;
        this.mediaGalleryRouter = mediaGalleryRouter;
        this.videoPlayerRouter = videoPlayerRouter;
        this.listingsView = view;
        this.linkView = linkView;
        this.commentView = commentView;
        this.privateMessageView = messageView;
    }

    public List<Listing> getListings() {
        return listings;
    }

    public boolean hasData() {
        return listings.size() != 0;
    }

    public void clearData() {
        listings.clear();
        listingsView.notifyDataSetChanged();
    }

    public void refreshData() {
        prevPageListingId = null;
        nextPageListingId = null;
        int numItems = listings.size();
        listings.clear();
        listingsView.notifyItemRangeRemoved(0, numItems);
        getNextData();
    }

    public void getPreviousData() {
        if (!beforeRequested) {
            if (networkConnectivityManager.isConnectedToNetwork()) {
                requestPreviousData();
            } else {
                String message = context.getString(R.string.error_network_unavailable);
                mainView.showToast(message);
            }
        }
    }

    public void getNextData() {
        if (!nextRequested) {
            if (networkConnectivityManager.isConnectedToNetwork()) {
                requestNextData();
            } else {
                String message = context.getString(R.string.error_network_unavailable);
                mainView.showToast(message);
            }
        }
    }

    protected abstract void requestPreviousData();

    protected abstract void requestNextData();

    @Override
    public void onFirstItemShown() {
        if (!beforeRequested && hasPreviousListings()) {
            Timber.d("Get PREVIOUS data");
            getPreviousData();
        }
    }

    @Override
    public void onLastItemShown() {
        if (!nextRequested && hasNextListings()) {
            Timber.d("Get NEXT data");
            getNextData();
        }
    }

    public void setData(@NotNull List<Listing> data) {
        listings.clear();
        listings.addAll(data);
    }

    public int getNumListings() {
        return listings.size();
    }

    public Listing getListingAt(int position) {
        return listings.get(position);
    }

    public boolean hasPreviousListings() {
        return prevPageListingId != null;
    }

    public boolean hasNextListings() {
        return nextPageListingId != null;
    }

    public boolean getShowControversiality() {
        return settingsManager.getShowControversiality();
    }

    protected void onListingsLoaded(ListingResponse response, boolean append) {
        mainView.dismissSpinner();

        if (append) nextRequested = false;
        else beforeRequested = false;

        if (response == null) {
            mainView.showToast(context.getString(R.string.error_xxx));
            return;
        }

        ListingResponseData data = response.getData();
        List<Listing> listings = data.getChildren();

        Timber.i("Loaded %d listings", listings.size());

        if (append) {
            int lastIndex = this.listings.size() - 1;
            this.listings.addAll(listings);
            nextPageListingId = data.getAfter();
            listingsView.notifyItemRangeInserted(lastIndex + 1, listings.size());
        } else {
            this.listings.addAll(0, listings);
            prevPageListingId = listings.size() == 0 ? null : listings.get(0).getFullName();
            listingsView.notifyItemRangeInserted(0, listings.size());
        }
    }

    protected ObservableSource<ListingResponse> checkNullResponse(ListingResponse listingResponse) {
        if (listingResponse.getData().getChildren() == null) {
            prevPageListingId = null;
            nextPageListingId = null;
            return Observable.error(new NullPointerException("no links"));
        } else {
            return Observable.just(listingResponse);
        }
    }

    // Keep in sync with impl in LinkCommentsPresenter
    // TODO: Eventually consolidate routing so we don't have duplicate routing for Links
    public void openLink(@NotNull Link link) {
        if (link.isSelf() && link.getSubreddit() != null) {
            linkCommentsRouter.showCommentsForLink(link.getSubreddit(), link.getId(), null);
            return;
        }

        // Determine correct routing for link
        if (link.isGallery()) {
            final List<GalleryItem> galleryItems = link.getGalleryItems();
            mediaGalleryRouter.openLinkGallery(galleryItems);
            return;
        }

        final Media media = link.getMedia();
        if (media != null) {
            final Media.RedditVideo redditVideo = media.getRedditVideo();
            if (redditVideo != null) {
                videoPlayerRouter.openRedditVideo(redditVideo);
                return;
            }
        }

        final String linkUrl = link.getUrl();
        if (linkUrl != null) {
            appRouter.openUrl(linkUrl);
        }
    }

    public void showCommentsForLink(@NotNull Link link) {
        linkCommentsRouter.showCommentsForLink(link.getSubreddit(), link.getId(), null);
    }

    public void replyToLink(Link link) {
        commentView.openReplyView(link);
    }

    public void upvoteLink(@NotNull Link link) {
        int dir = (link.getLiked() == null || !link.getLiked()) ? 1 : 0;
        vote(link, link.getId(), dir);
    }

    public void downvoteLink(@NotNull Link link) {
        int dir = (link.getLiked() == null || link.getLiked()) ? -1 : 0;
        vote(link, link.getId(), dir);
    }

    public void saveLink(@NotNull Link link) {
        if (!redditService.isUserAuthorized()) {
            mainView.showToast(context.getString(R.string.user_required));
            return;
        }
        save(link, true);
    }

    public void unsaveLink(@NotNull Link link) {
        if (!redditService.isUserAuthorized()) {
            mainView.showToast(context.getString(R.string.user_required));
            return;
        }
        save(link, false);
    }

    public void shareLink(@NotNull Link link) {
        linkView.openShareView(link);
    }

    public void openLinkSubreddit(@NotNull Link link) {
        String subreddit = link.getSubreddit();
        linkView.openSubredditView(subreddit);
    }

    public void openLinkUserProfile(@NotNull Link link) {
        linkView.openUserProfileView(link);
    }

    public void openLinkInBrowser(@NotNull Link link) {
        linkView.openLinkInBrowser(link);
    }

    public void openCommentsInBrowser(@NotNull Link link) {
        linkView.openCommentsInBrowser(link);
    }

    public void hideLink(@NotNull Link link) {
        if (!redditService.isUserAuthorized()) {
            mainView.showToast(context.getString(R.string.user_required));
            return;
        }

        hide(link, true);
    }

    public void unhideLink(@NotNull Link link) {
        if (!redditService.isUserAuthorized()) {
            mainView.showToast(context.getString(R.string.user_required));
            return;
        }

        hide(link, false);
    }

    public void reportLink(@NotNull Link link) {
        if (!redditService.isUserAuthorized()) {
            mainView.showToast(context.getString(R.string.user_required));
        } else {
            linkView.openReportView(link);
        }
    }

    public void showCommentThread(
            @NotNull String subreddit, @NotNull String linkId, @NotNull String commentId) {
        linkCommentsRouter.showCommentsForLink(subreddit, linkId, commentId);
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
        if (comment.getArchived()) {
            mainView.showToast(context.getString(R.string.listing_archived));
        } else {
            commentView.openReplyView(comment);
        }
    }

    public void upvoteComment(@NotNull Comment comment) {
        int dir = (comment.getLiked() == null || !comment.getLiked()) ? 1 : 0;
        vote(comment, comment.getId(), dir);
    }

    public void downvoteComment(@NotNull Comment comment) {
        int dir = (comment.getLiked() == null || comment.getLiked()) ? -1 : 0;
        vote(comment, comment.getId(), dir);
    }

    public void saveComment(@NotNull Comment comment) {
        if (!redditService.isUserAuthorized()) {
            mainView.showToast(context.getString(R.string.user_required));
            return;
        }
        save(comment, true);
    }

    public void unsaveComment(@NotNull Comment comment) {
        if (!redditService.isUserAuthorized()) {
            mainView.showToast(context.getString(R.string.user_required));
            return;
        }
        save(comment, false);
    }

    public void shareComment(@NotNull Comment comment) {
        commentView.openShareView(comment);
    }

    public void openCommentUserProfile(@NotNull Comment comment) {
        commentView.openUserProfileView(comment);
    }

    public void openCommentInBrowser(@NotNull Comment comment) {
        commentView.openCommentInBrowser(comment);
    }

    public void reportComment(@NotNull Comment comment) {
        if (comment.getArchived()) {
            mainView.showToast(context.getString(R.string.listing_archived));
        } else if (!redditService.isUserAuthorized()) {
            mainView.showToast(context.getString(R.string.user_required));
        } else {
            commentView.openReportView(comment);
        }
    }

    public void openCommentLink(@NotNull Comment comment) {
        linkCommentsRouter.showCommentsForLink(comment.getSubreddit(), comment.getLinkId(), null);
    }

    public UserIdentity getAuthorizedUser() {
        return identityManager.getUserIdentity();
    }

    public void onSortChanged() {
        refreshData();
    }

    private void vote(Votable votable, String votableId, int direction) {
        if (votable.getArchived()) {
            mainView.showToast(context.getString(R.string.listing_archived));
        } else if (!redditService.isUserAuthorized()) {
            mainView.showToast(context.getString(R.string.user_required));
        } else {
            redditService.vote(votable.getKind() + "_" + votableId, direction)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            () -> {
                                votable.applyVote(direction);
                                listingsView.notifyItemChanged(getIndexOf((Listing) votable));
                            },
                            error -> {
                                if (error instanceof IOException) {
                                    String message = context.getString(R.string.error_network_unavailable);
                                    mainView.showError(message);
                                } else {
                                    Timber.w(error, "Error voting on listing");
                                    String message = context.getString(R.string.vote_failed);
                                    mainView.showError(message);
                                }
                            }
                    );
        }
    }

    /**
     * This is overridden in link comments view which has headers
     */
    protected int getIndexOf(Listing listing) {
        return listings.indexOf(listing);
    }

    private void save(Savable savable, boolean toSave) {
        redditService.save(savable.getFullName(), null, toSave)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            savable.setSaved(toSave);
                            listingsView.notifyItemChanged(getIndexOf((Listing) savable));
                        },
                        error -> {
                            if (error instanceof IOException) {
                                String message = context.getString(R.string.error_network_unavailable);
                                mainView.showError(message);
                            } else {
                                Timber.w(error, "Error saving listing");
                                String message = context.getString(R.string.save_failed);
                                mainView.showError(message);
                            }
                        }
                );
    }

    private void hide(Hideable hideable, boolean toHide) {
        redditService.hide(hideable.getFullName(), toHide)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            int pos = getIndexOf((Listing) hideable);
                            listings.remove(pos);
                            listingsView.notifyItemRemoved(pos);
                        },
                        error -> {
                            if (error instanceof IOException) {
                                String message = context.getString(R.string.error_network_unavailable);
                                mainView.showError(message);
                            } else {
                                Timber.w(error, "Error hiding listing");
                                String message = context.getString(R.string.hide_failed);
                                mainView.showError(message);
                            }
                        }
                );
    }

    public boolean shouldShowNsfwTag() {
        final boolean isNsfwSubreddit = subreddit != null && subreddit.isOver18();
        final boolean hideNsfwInSettings =
                settingsManager.getNoProfanity() || settingsManager.getLabelNsfw();
        final boolean userOver18 = settingsManager.getOver18();
        return !userOver18 || !isNsfwSubreddit && hideNsfwInSettings;
    }

    public ThumbnailMode getThumbnailMode() {
        if (settingsManager.getOver18()) {
            if (subreddit != null && subreddit.isOver18()) {
                return ThumbnailMode.FULL;
            } else {
                if (settingsManager.getNoProfanity()) {
                    return ThumbnailMode.VARIANT;
                } else {
                    if (settingsManager.getLabelNsfw()) {
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
        return identityManager.getUserIdentity();
    }

    public void replyToMessage(@NotNull PrivateMessage message) {
        mainView.showToast(context.getString(R.string.implementation_pending));
    }

    public void markMessageRead(@NotNull PrivateMessage pm) {
        String fullname = pm.getFullName();
        redditService.markMessagesRead(fullname)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            pm.markUnread(false);
                            listingsView.notifyItemChanged(getIndexOf(pm));
                        },
                        error -> {
                            if (error instanceof IOException) {
                                String message = context.getString(R.string.error_network_unavailable);
                                mainView.showError(message);
                            } else {
                                Timber.w(error, "Error marking message read");
                                String errorMessage = context.getString(R.string.error_xxx);
                                mainView.showError(errorMessage);
                            }
                        }
                );
    }

    public void markMessageUnread(@NotNull PrivateMessage pm) {
        String fullname = pm.getFullName();
        redditService.markMessagesUnread(fullname)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            pm.markUnread(true);
                            listingsView.notifyItemChanged(getIndexOf(pm));
                        },
                        error -> {
                            if (error instanceof IOException) {
                                String message = context.getString(R.string.error_network_unavailable);
                                mainView.showError(message);
                            } else {
                                Timber.w(error, "Error marking message unread");
                                String errorMessage = context.getString(R.string.error_xxx);
                                mainView.showError(errorMessage);
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
        appRouter.showInboxMessages(messages);
    }

    public void reportMessage(@NotNull PrivateMessage message) {
        privateMessageView.openReportView(message);
    }

    public void blockUser(@NotNull PrivateMessage message) {
        mainView.showToast(context.getString(R.string.implementation_pending));
    }
}
