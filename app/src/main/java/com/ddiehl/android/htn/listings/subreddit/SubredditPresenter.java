package com.ddiehl.android.htn.listings.subreddit;

import androidx.annotation.NonNull;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.gallery.MediaGalleryRouter;
import com.ddiehl.android.htn.listings.BaseListingsPresenter;
import com.ddiehl.android.htn.listings.comments.LinkCommentsRouter;
import com.ddiehl.android.htn.navigation.AppNavigationMenuHelper;
import com.ddiehl.android.htn.navigation.RedditNavigationView;
import com.ddiehl.android.htn.routing.AppRouter;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.video.VideoPlayerRouter;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import rxreddit.api.NoSuchSubredditException;
import rxreddit.model.Link;
import rxreddit.model.ListingResponse;
import rxreddit.model.Subreddit;
import timber.log.Timber;

public class SubredditPresenter extends BaseListingsPresenter {

    private final AppNavigationMenuHelper appNavigationMenuHelper;
    private SubredditView subredditView;

    public SubredditPresenter(
            MainView main,
            RedditNavigationView navigationView,
            AppRouter appRouter,
            LinkCommentsRouter linkCommentsRouter,
            MediaGalleryRouter mediaGalleryRouter,
            VideoPlayerRouter videoPlayerRouter,
            AppNavigationMenuHelper appNavigationMenuHelper,
            SubredditView view) {
        super(
                main,
                navigationView,
                appRouter,
                linkCommentsRouter,
                mediaGalleryRouter,
                videoPlayerRouter,
                view,
                view,
                null,
                null
        );
        this.appNavigationMenuHelper = appNavigationMenuHelper;
        this.subredditView = view;
    }

    @Override
    protected void requestPreviousData() {
        String subreddit = subredditView.getSubreddit();
        if (subreddit != null
                && !subreddit.equals("all")
                && !subreddit.equals("random")
                && this.subreddit == null) {
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
                && this.subreddit == null) {
            loadSubredditInfo(subredditView.getSubreddit());
        } else {
            getSubredditLinks(true);
        }
    }

    public Subreddit getSubreddit() {
        return subreddit;
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
                        this::onSubredditLoaded,
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
                        response -> onSubredditLinksLoaded(response, subreddit, append),
                        error -> onSubredditLinkLoadError(error)
                );
    }

    private void onSubredditLinksLoaded(ListingResponse response, String subreddit, boolean append) {
        onListingsLoaded(response, append);

        if ("random".equals(subreddit)) {
            Link link = getLinkFromListingResponse(response);
            String randomSubreddit = link == null ? null : link.getSubreddit();
            subredditView.onRandomSubredditLoaded(randomSubreddit);
        }
    }

    private void onSubredditLinkLoadError(Throwable error) {
        if (error instanceof IOException) {
            String message = context.getString(R.string.error_network_unavailable);
            mainView.showError(message);
        } else if (error instanceof NoSuchSubredditException) {
            String message = context.getString(R.string.error_no_such_subreddit);
            mainView.showError(message);
        } else {
            Timber.w(error, "Error loading links");
            String message = context.getString(R.string.error_get_links);
            mainView.showError(message);
        }
    }

    private Link getLinkFromListingResponse(@NotNull ListingResponse response) {
        if (response.getData() == null) return null;

        if (response.getData().getChildren() == null) return null;

        if (response.getData().getChildren().size() == 0) return null;

        if (response.getData().getChildren().get(0) == null) return null;

        if (!(response.getData().getChildren().get(0) instanceof Link)) return null;

        return (Link) response.getData().getChildren().get(0);
    }

    private void onSubredditLoaded(@NonNull Subreddit subreddit) {
        this.subreddit = subreddit;

        boolean over18 = settingsManager.getOver18();
        if (shouldShowNsfwDialog(this.subreddit, over18)) {
            subredditView.showNsfwWarningDialog();
        } else {
            if (this.subreddit != null) {
                requestNextData();
                subredditView.refreshOptionsMenu();
            } else {
                String message = context.getString(R.string.error_private_subreddit);
                mainView.showToast(message);
            }
        }

        final String headerImageUrl = subreddit.getHeaderImageUrl();
        appNavigationMenuHelper.loadImageIntoDrawerHeader(headerImageUrl);
    }

    private boolean shouldShowNsfwDialog(Subreddit subreddit, boolean userOver18) {
        return subreddit != null && subreddit.isOver18() && !userOver18;
    }

    public void subscribeToSubreddit(String subredditName, boolean subscribe) {
        if (subscribe) {
            redditService.subscribe(subredditName)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> onSubredditSubscribed(true), Timber::e);
        } else {
            redditService.unsubscribe(subredditName)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> onSubredditSubscribed(false), Timber::e);
        }
    }

    private void onSubredditSubscribed(boolean isSubscribed) {
        subreddit.setUserIsSubscriber(isSubscribed);
        subredditView.refreshOptionsMenu();
        mainView.showToast(isSubscribed ? R.string.subscribed : R.string.unsubscribed);
    }
}
