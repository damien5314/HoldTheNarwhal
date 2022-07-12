package com.ddiehl.android.htn.listings.comments;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.gallery.MediaGalleryRouter;
import com.ddiehl.android.htn.listings.BaseListingsPresenter;
import com.ddiehl.android.htn.navigation.RedditNavigationView;
import com.ddiehl.android.htn.routing.AppRouter;
import com.ddiehl.android.htn.utils.RedditUtilKt;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.video.VideoPlayerRouter;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import rxreddit.model.AbsComment;
import rxreddit.model.Comment;
import rxreddit.model.CommentStub;
import rxreddit.model.GalleryItem;
import rxreddit.model.Link;
import rxreddit.model.Listing;
import rxreddit.model.ListingResponse;
import rxreddit.model.Media;
import rxreddit.model.MoreChildrenResponse;
import timber.log.Timber;

public class LinkCommentsPresenter extends BaseListingsPresenter {

    private static final int MAX_CHILDREN_PER_REQUEST = 20;

    private final AppRouter appRouter;
    private final MediaGalleryRouter mediaGalleryRouter;
    private final VideoPlayerRouter videoPlayerRouter;
    private final LinkCommentsView linkCommentsView;
    private final CommentBank commentBank;
    private Link linkContext;
    private Listing replyTarget = null;
    private boolean dataRequested = false;

    public LinkCommentsPresenter(
            MainView main,
            RedditNavigationView navigationView,
            AppRouter appRouter,
            LinkCommentsRouter linkCommentsRouter,
            MediaGalleryRouter mediaGalleryRouter,
            VideoPlayerRouter videoPlayerRouter,
            LinkCommentsView view) {
        super(
                main,
                navigationView,
                appRouter,
                linkCommentsRouter,
                mediaGalleryRouter,
                videoPlayerRouter,
                view,
                view,
                view,
                null
        );
        this.appRouter = appRouter;
        this.mediaGalleryRouter = mediaGalleryRouter;
        this.videoPlayerRouter = videoPlayerRouter;
        linkCommentsView = view;
        commentBank = new CommentBankList();
    }

    @Override
    public boolean hasData() {
        return commentBank.size() != 0;
    }

    @Override
    public void clearData() {
        linkContext = null;
        commentBank.clear();
    }

    @Override
    public void refreshData() {
        int numItems = getListings().size();
        commentBank.clear();
        linkCommentsView.notifyItemRangeRemoved(0, numItems);
        getNextData();
    }

    @Override
    protected void requestPreviousData() {
        // Comments aren't paginated the same as a listings endpoint
    }

    @Override
    protected void requestNextData() {
        if (!dataRequested) {
            dataRequested = true;
            redditService.loadLinkComments(
                            linkCommentsView.getSubreddit(), linkCommentsView.getArticleId(),
                            linkCommentsView.getSort(), linkCommentsView.getCommentId()
                    )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(disposable -> mainView.showSpinner())
                    .doFinally(() -> {
                        dataRequested = false;
                        mainView.dismissSpinner();
                    })
                    .subscribe(
                            this::onLoadLinkComments,
                            error -> {
                                if (error instanceof IOException) {
                                    String message = context.getString(R.string.error_network_unavailable);
                                    mainView.showError(message);
                                } else {
                                    Timber.w(error, "Error retrieving comment listings");
                                    String message = context.getString(R.string.error_get_link_comments);
                                    mainView.showError(message);
                                }
                            }
                    );
        }
    }

    private void onLoadLinkComments(List<ListingResponse> listingResponseList) {
        if (listingResponseList == null) return;

        linkCommentsView.refreshOptionsMenu();

        // Get link
        ListingResponse linkResponse = listingResponseList.get(0);
        linkContext = (Link) linkResponse.getData().getChildren().get(0);
        Timber.i("Link: %s", linkContext.getFullName());

        // Get comments and flatten the comment tree
        ListingResponse commentsResponse = listingResponseList.get(1);
        List<Listing> comments = commentsResponse.getData().getChildren();
        RedditUtilKt.flattenCommentList(comments);
        Timber.i("Comments: %d", comments.size());

        // Add comments to CommentBank
        commentBank.clear();
        commentBank.addAll(comments);

        // Collapse all threads under the user's minimum score
        Integer minScore = settingsManager.getMinCommentScore();
        commentBank.collapseAllThreadsUnder(minScore);

        // Notify adapter
        // TODO Specify commentsAdded
        linkCommentsView.notifyDataSetChanged();
    }

    @Override
    public void getMoreComments(@NotNull CommentStub parentStub) {
        List<String> children = parentStub.getChildren();
        // Truncate list of children to 20
        children = children.subList(0, Math.min(MAX_CHILDREN_PER_REQUEST, children.size()));
        redditService.loadMoreChildren(linkContext.getId(), children, linkCommentsView.getSort())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> mainView.showSpinner())
                .doFinally(mainView::dismissSpinner)
                .subscribe(
                        response -> onLoadMoreChildren(response, parentStub),
                        error -> {
                            if (error instanceof IOException) {
                                String message = context.getString(R.string.error_network_unavailable);
                                mainView.showError(message);
                            } else {
                                Timber.w(error, "Error retrieving more comments");
                                String message = context.getString(R.string.error_get_more_comments);
                                mainView.showError(message);
                            }
                        }
                );
    }

    private void onLoadMoreChildren(MoreChildrenResponse response, @NotNull CommentStub parentStub) {
        final int stubIndex = commentBank.indexOf(parentStub);
        if (stubIndex == -1) {
            // Parent comment stub is no longer in the list, we've probably already processed this response
            return;
        }

        List<Listing> comments = response.getChildComments();

        if (comments == null || comments.size() == 0) {
            commentBank.remove(parentStub);
        } else {
            Timber.i("More comments: %d", comments.size());

            setDepthForCommentsList(comments, parentStub);

            parentStub.removeChildren(comments);
            parentStub.setCount(parentStub.getChildren().size());

            if (parentStub.getCount() == 0) {
                commentBank.remove(stubIndex);
            }

            commentBank.addAll(stubIndex, comments);
        }

        Integer minScore = settingsManager.getMinCommentScore();
        commentBank.collapseAllThreadsUnder(minScore);

        // TODO Specify commentRemoved and commentsAdded
        linkCommentsView.notifyDataSetChanged();
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
        return linkContext;
    }

    @Override
    public AbsComment getListingAt(int position) {
        return commentBank.getVisibleComment(position);
    }

    public void toggleThreadVisible(Comment comment) {
        int before = commentBank.getNumVisible();
        int position = commentBank.visibleIndexOf(comment);
        commentBank.toggleThreadVisible(comment);
        int diff = commentBank.getNumVisible() - before;
        linkCommentsView.notifyItemChanged(position);
        if (diff > 0) {
            linkCommentsView.notifyItemRangeInserted(position + 1, diff);
        } else { // diff < 0
            linkCommentsView.notifyItemRangeRemoved(position + 1, diff * -1);
        }
    }

    @Override
    public int getNumListings() {
        return commentBank.getNumVisible();
    }

    @Override
    protected int getIndexOf(Listing listing) {
        if (listing instanceof AbsComment)
            return commentBank.visibleIndexOf((AbsComment) listing);
        else return -1;
    }

    // Keep in sync with impl in BaseListingsPresenter
    // TODO: Eventually consolidate routing so we don't have duplicate routing for Links
    @Override
    public void openLink(@NotNull Link link) {
        if (link.isSelf()) {
            // Overriding this so we don't keep opening link comments over and over
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

        if (link.getUrl() != null) {
            linkCommentsView.openUrlInWebView(link.getUrl());
        }
    }

    @Override
    public void replyToLink(Link link) {
        if (link.getArchived()) {
            mainView.showToast(context.getString(R.string.listing_archived));
        } else if (!redditService.isUserAuthorized()) {
            mainView.showToast(context.getString(R.string.user_required));
        } else {
            replyTarget = link;
            linkCommentsView.openReplyView(link);
        }
    }

    @Override
    public void replyToComment(@NotNull Comment comment) {
        if (comment.getArchived()) {
            mainView.showToast(context.getString(R.string.listing_archived));
        } else if (!redditService.isUserAuthorized()) {
            mainView.showToast(context.getString(R.string.user_required));
        } else {
            replyTarget = comment;
            linkCommentsView.openReplyView(comment);
        }
    }

    public void onCommentSubmitted(@NotNull String commentText) {
        String parentId = replyTarget.getFullName();
        redditService.addComment(parentId, commentText)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        comment -> {
                            String message = context.getString(R.string.comment_added);
                            mainView.showToast(message);

                            // TODO Optimize this logic, it probably takes a long time in large threads
                            int position;
                            if (parentId.startsWith("t1_")) { // Comment
                                comment.setDepth(((Comment) replyTarget).getDepth() + 1);
                                position = commentBank.indexOf((Comment) replyTarget) + 1;
                            } else {
                                comment.setDepth(1);
                                position = 0;
                            }
                            commentBank.add(position, comment);
                            linkCommentsView.notifyItemInserted(
                                    commentBank.visibleIndexOf(comment)
                            );
                        },
                        error -> {
                            if (error instanceof IOException) {
                                String message = context.getString(R.string.error_network_unavailable);
                                mainView.showError(message);
                            } else {
                                Timber.w(error, "Error adding comment");
                                String message = context.getString(R.string.error_add_comment);
                                mainView.showError(message);
                            }
                        }
                );
    }

    @Override
    public void openCommentLink(@NotNull Comment comment) {
        // Link is already being displayed with this presenter
    }

    public boolean shouldShowParentLink() {
        return linkCommentsView.getCommentId() != null;
    }
}
