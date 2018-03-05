package com.ddiehl.android.htn.listings.inbox;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.listings.BaseListingsPresenter;
import com.ddiehl.android.htn.navigation.RedditNavigationView;
import com.ddiehl.android.htn.view.MainView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import rxreddit.model.Listing;
import rxreddit.model.ListingResponse;
import rxreddit.model.PrivateMessage;
import timber.log.Timber;

public class InboxPresenter extends BaseListingsPresenter {

    private final InboxView inboxView;

    public InboxPresenter(MainView main, RedditNavigationView navigationView, InboxView inbox) {
        super(main, navigationView, inbox, inbox, inbox, inbox);
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
                    .subscribe(() -> { }, Timber::e);
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
