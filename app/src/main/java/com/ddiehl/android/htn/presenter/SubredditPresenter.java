package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.RedditNavigationView;
import com.ddiehl.android.htn.view.SubredditView;

import java.io.IOException;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rxreddit.model.Link;
import rxreddit.model.ListingResponse;
import rxreddit.model.Subreddit;
import timber.log.Timber;

public class SubredditPresenter extends BaseListingsPresenter {

    private SubredditView mSubredditView;

    public SubredditPresenter(MainView main, RedditNavigationView navigationView, SubredditView view) {
        super(main, navigationView, view, view, null, null);
        mSubredditView = view;
    }

    @Override
    void requestPreviousData() {
        String subreddit = mSubredditView.getSubreddit();
        if (subreddit != null
                && !subreddit.equals("all")
                && !subreddit.equals("random")
                && mSubredditInfo == null) {
            loadSubredditInfo(mSubredditView.getSubreddit());
        } else {
            getSubredditLinks(false);
        }
    }

    @Override
    public void requestNextData() {
        String subreddit = mSubredditView.getSubreddit();
        if (subreddit != null
                && !subreddit.equals("all")
                && !subreddit.equals("random")
                && mSubredditInfo == null) {
            loadSubredditInfo(mSubredditView.getSubreddit());
        } else {
            getSubredditLinks(true);
        }
    }

    public Subreddit getSubredditInfo() {
        return mSubredditInfo;
    }

    private void loadSubredditInfo(@NonNull String subreddit) {
        mRedditService.getSubredditInfo(subreddit)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(mMainView::showSpinner)
                .doOnTerminate(() -> {
                    mMainView.dismissSpinner();
                    mNextRequested = false;
                })
                .subscribe(
                        onSubredditInfoLoaded(),
                        error -> {
                            if (error instanceof IOException) {
                                String message = mContext.getString(R.string.error_network_unavailable);
                                mMainView.showError(message);
                            } else {
                                Timber.w(error, "Error loading subreddit info");
                                String message = mContext.getString(R.string.error_get_subreddit_info);
                                mMainView.showError(message);
                            }
                        }
                );
    }

    private void getSubredditLinks(boolean append) {
        mAnalytics.logLoadSubreddit(
                mSubredditView.getSubreddit(), mSubredditView.getSort(), mSubredditView.getTimespan());

        final String before = append ? null : mPrevPageListingId;
        final String after = append ? mNextPageListingId : null;
        final String subreddit = mSubredditView.getSubreddit();

        mRedditService.loadLinks(subreddit, mSubredditView.getSort(), mSubredditView.getTimespan(), before, after)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> {
                    mMainView.showSpinner();
                    if (append) mNextRequested = true;
                    else mBeforeRequested = true;
                })
                .doOnTerminate(() -> {
                    mMainView.dismissSpinner();
                    if (append) mNextRequested = false;
                    else mBeforeRequested = false;
                })
                .subscribe(
                        response -> {
                            onListingsLoaded(append).call(response);

                            if ("random".equals(subreddit)) {
                                Link link = getLinkFromListingResponse(response);
                                String randomSubreddit = link == null ? null : link.getSubreddit();
                                mSubredditView.onRandomSubredditLoaded(randomSubreddit);
                            }
                        },
                        error -> {
                            if (error instanceof IOException) {
                                String message = mContext.getString(R.string.error_network_unavailable);
                                mMainView.showError(message);
                            } else {
                                Timber.w(error, "Error loading links");
                                String message = mContext.getString(R.string.error_get_links);
                                mMainView.showError(message);
                            }
                        }
                );
    }

    private Link getLinkFromListingResponse(@NonNull ListingResponse response) {
        if (response.getData() == null) return null;

        if (response.getData().getChildren() == null) return null;

        if (response.getData().getChildren().size() == 0) return null;

        if (response.getData().getChildren().get(0) == null) return null;

        if (!(response.getData().getChildren().get(0) instanceof Link)) return null;

        return (Link) response.getData().getChildren().get(0);
    }

    private Action1<Subreddit> onSubredditInfoLoaded() {
        return info -> {
            mSubredditInfo = info;

            boolean over18 = mSettingsManager.getOver18();
            if (shouldShowNsfwDialog(mSubredditInfo, over18)) {
                mSubredditView.showNsfwWarningDialog();
            } else {
                if (mSubredditInfo != null) {
                    requestNextData();
                } else {
                    String message = mContext.getString(R.string.error_private_subreddit);
                    mMainView.showToast(message);
                }
            }

            mSubredditView.loadHeaderImage();
        };
    }

    private boolean shouldShowNsfwDialog(Subreddit subreddit, boolean userOver18) {
        return subreddit != null && subreddit.isOver18() && !userOver18;
    }
}
