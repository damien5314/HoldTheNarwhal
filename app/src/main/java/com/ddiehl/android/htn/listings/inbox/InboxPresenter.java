package com.ddiehl.android.htn.listings.inbox;

import androidx.fragment.app.FragmentActivity;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.gallery.MediaGalleryRouter;
import com.ddiehl.android.htn.listings.BaseListingsPresenter;
import com.ddiehl.android.htn.listings.comments.AddCommentDialogRouter;
import com.ddiehl.android.htn.listings.comments.LinkCommentsRouter;
import com.ddiehl.android.htn.listings.report.ReportViewRouter;
import com.ddiehl.android.htn.routing.AppRouter;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.video.VideoPlayerRouter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import rxreddit.model.Listing;
import rxreddit.model.ListingResponse;
import rxreddit.model.PrivateMessage;
import timber.log.Timber;

public class InboxPresenter extends BaseListingsPresenter {

    private final MainView mainView;
    private final InboxView inboxView;

    @Inject
    public InboxPresenter(
            FragmentActivity activity,
            MainView main,
            AppRouter appRouter,
            LinkCommentsRouter linkCommentsRouter,
            MediaGalleryRouter mediaGalleryRouter,
            VideoPlayerRouter videoPlayerRouter,
            AddCommentDialogRouter addCommentDialogRouter,
            ReportViewRouter reportViewRouter,
            InboxView inbox
    ) {
        super(
                activity,
                main,
                appRouter,
                linkCommentsRouter,
                mediaGalleryRouter,
                videoPlayerRouter,
                addCommentDialogRouter,
                reportViewRouter,
                inbox
        );
        mainView = main;
        inboxView = inbox;
    }

    @Override
    protected void requestPreviousData() {
        requestData(false);
    }

    @Override
    protected void requestNextData() {
        requestData(true);
    }

    private void requestData(boolean append) {
        String show = inboxView.getShow();
        Timber.i("Getting inbox (%s)", show);

        String prevId = append ? null : prevPageListingId;
        String nextId = append ? nextPageListingId : null;

        redditService.getInbox(show, true, prevId, nextId)
                .flatMap(this::checkNullResponse)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> {
                    mainView.showSpinner();
                    nextRequested = true;
                })
                .doFinally(() -> {
                    mainView.dismissSpinner();
                    nextRequested = false;
                })
                .subscribe(
                        listings -> {
                            onListingsLoaded(listings, append);
                            markMessagesRead(listings);
                        },
                        error -> {
                            if (error instanceof IOException) {
                                String message = context.getString(R.string.error_network_unavailable);
                                mainView.showError(message);
                            } else {
                                Timber.w(error, "Error retrieving inbox listings");
                                String message = context.getString(R.string.error_get_inbox);
                                mainView.showError(message);
                            }
                        }
                );
    }

    public void requestData() {
        refreshData();
    }

    public void onMarkMessagesRead() {
        redditService.markAllMessagesRead()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            for (Listing listing : getListings()) {
                                if (listing instanceof PrivateMessage) {
                                    ((PrivateMessage) listing).markUnread(false);
                                }
                            }
                            inboxView.notifyDataSetChanged();
                        },
                        error -> {
                            if (error instanceof IOException) {
                                String message = context.getString(R.string.error_network_unavailable);
                                mainView.showError(message);
                            } else {
                                Timber.w(error, "Error marking messages read");
                                String message = context.getString(R.string.error_xxx);
                                mainView.showError(message);
                            }
                        }
                );
    }

    public void onViewSelected(String show) {
        refreshData();
    }

    private void markMessagesRead(ListingResponse listings) {
        final List<String> messageFullnames = new ArrayList<>();
        for (Listing listing : listings.getData().getChildren()) {
            if (listing instanceof PrivateMessage
                    && ((PrivateMessage) listing).isUnread()) {
                messageFullnames.add(listing.getFullName());
            }
        }
        if (!messageFullnames.isEmpty()) {
            final String commaSeparated = getCommaSeparatedString(messageFullnames);
            redditService.markMessagesRead(commaSeparated)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                    }, Timber::e);
        }
    }

    private String getCommaSeparatedString(List<String> strings) {
        final StringBuilder result = new StringBuilder();
        for (String string : strings) {
            result.append(string).append(",");
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }
}
