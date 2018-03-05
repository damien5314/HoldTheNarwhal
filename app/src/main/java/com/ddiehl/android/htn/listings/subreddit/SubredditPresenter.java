package com.ddiehl.android.htn.listings.subreddit;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.listings.BaseListingsPresenter;
import com.ddiehl.android.htn.navigation.RedditNavigationView;
import com.ddiehl.android.htn.view.MainView;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import rxreddit.model.Link;
import rxreddit.model.ListingResponse;
import rxreddit.model.Subreddit;
import timber.log.Timber;

public class SubredditPresenter extends BaseListingsPresenter {

    private SubredditView subredditView;

    public SubredditPresenter(MainView main, RedditNavigationView navigationView, SubredditView view) {
        super(main, navigationView, view, view, null, null);
        subredditView = view;
    }

    @Override
    protected void requestPreviousData() {
        String subreddit = subredditView.getSubreddit();
        if (subreddit != null
                && !subreddit.equals("all")
                && !subreddit.equals("random")
                && subredditInfo == null) {
            loadSubredditInfo(subredditView.getSubreddit());
        } else {
            getSubredditLinks(false);
        }
    }

    @Override
    public void requestNextData() {
        String subreddit = subredditView.getSubreddit();
        if (subreddit != null
                && !subreddit.equals("all")
                && !subreddit.equals("random")
                && subredditInfo == null) {
            loadSubredditInfo(subredditView.getSubreddit());
        } else {
            getSubredditLinks(true);
        }
    }

    public Subreddit getSubredditInfo() {
        return subredditInfo;
    }

    private void loadSubredditInfo(@NotNull String subreddit) {
        redditService.getSubredditInfo(subreddit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> mainView.showSpinner())
                .doFinally(() -> {
                    mainView.dismissSpinner();
                    nextRequested = false;
                })
                .subscribe(
                        onSubredditInfoLoaded(),
                        error -> {
                            if (error instanceof IOException) {
                                String message = context.getString(R.string.error_network_unavailable);
                                mainView.showError(message);
                            } else {
                                Timber.w(error, "Error loading subreddit info");
                                String message = context.getString(R.string.error_get_subreddit_info);
                                mainView.showError(message);
                            }
                        }
                );
    }

    private void getSubredditLinks(boolean append) {
        final String before = append ? null : prevPageListingId;
        final String after = append ? nextPageListingId : null;
        final String subreddit = subredditView.getSubreddit();

        redditService.loadLinks(subreddit, subredditView.getSort(), subredditView.getTimespan(), before, after)
                .flatMap(this::checkNullResponse)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> {
                    mainView.showSpinner();
                    if (append) nextRequested = true;
                    else beforeRequested = true;
                })
                .doFinally(() -> {
                    mainView.dismissSpinner();
                    if (append) nextRequested = false;
                    else beforeRequested = false;
                })
                .subscribe(
                        response -> {
                            onListingsLoaded(response, append);

                            if ("random".equals(subreddit)) {
                                Link link = getLinkFromListingResponse(response);
                                String randomSubreddit = link == null ? null : link.getSubreddit();
                                subredditView.onRandomSubredditLoaded(randomSubreddit);
                            }
                        },
                        error -> {
                            if (error instanceof IOException) {
                                String message = context.getString(R.string.error_network_unavailable);
                                mainView.showError(message);
                            } else {
                                Timber.w(error, "Error loading links");
                                String message = context.getString(R.string.error_get_links);
                                mainView.showError(message);
                            }
                        }
                );
    }

    private Link getLinkFromListingResponse(@NotNull ListingResponse response) {
        if (response.getData() == null) return null;

        if (response.getData().getChildren() == null) return null;

        if (response.getData().getChildren().size() == 0) return null;

        if (response.getData().getChildren().get(0) == null) return null;

        if (!(response.getData().getChildren().get(0) instanceof Link)) return null;

        return (Link) response.getData().getChildren().get(0);
    }

    private Consumer<Subreddit> onSubredditInfoLoaded() {
        return info -> {
            subredditInfo = info;

            boolean over18 = settingsManager.getOver18();
            if (shouldShowNsfwDialog(subredditInfo, over18)) {
                subredditView.showNsfwWarningDialog();
            } else {
                if (subredditInfo != null) {
                    requestNextData();
                } else {
                    String message = context.getString(R.string.error_private_subreddit);
                    mainView.showToast(message);
                }
            }

            subredditView.loadHeaderImage();
        };
    }

    private boolean shouldShowNsfwDialog(Subreddit subreddit, boolean userOver18) {
        return subreddit != null && subreddit.isOver18() && !userOver18;
    }
}
