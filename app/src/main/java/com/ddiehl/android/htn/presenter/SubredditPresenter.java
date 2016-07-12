package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.RedditNavigationView;
import com.ddiehl.android.htn.view.SubredditView;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rxreddit.model.Link;
import rxreddit.model.ListingResponse;
import rxreddit.model.Subreddit;

public class SubredditPresenter extends BaseListingsPresenter implements LinkPresenter {

  private SubredditView mSubredditView;

  public SubredditPresenter(MainView main, RedditNavigationView navigationView, SubredditView view) {
    super(main, navigationView, view, view, null, null);
    mSubredditView = view;
  }

  @Override
  public void onResume() {
    super.onResume();
    loadHeaderImage();
  }

  @Override
  public void onPause() {
    mMainView.loadImageIntoDrawerHeader(null);
    super.onPause();
  }

  @Override
  void requestPreviousData() {
    String subreddit = mSubredditView.getSubreddit();
    if (subreddit != null
        && !subreddit.equals("all")
        && !subreddit.equals("random")
        && mSubredditInfo == null) {
      getSubredditInfo();
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
      getSubredditInfo();
    } else {
      getSubredditLinks(true);
    }
  }

  private void getSubredditInfo() {
    mRedditService.getSubredditInfo(mSubredditView.getSubreddit())
        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe(() -> mMainView.showSpinner(null))
        .doOnTerminate(() -> {
          mMainView.dismissSpinner();
          mNextRequested = false;
        })
        .subscribe(onSubredditInfoLoaded(),
            e -> {
              String message = mContext.getString(R.string.error_get_subreddit_info);
              mMainView.showError(e, message);
            });
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
          mMainView.showSpinner(null);
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
              String message = mContext.getString(R.string.error_get_links);
              mMainView.showError(error, message);
            });
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
        if (mSubredditInfo != null) requestNextData();
        else mMainView.showToast(mContext.getString(R.string.error_private_subreddit));
      }

      loadHeaderImage();
    };
  }

  private boolean shouldShowNsfwDialog(Subreddit subreddit, boolean userOver18) {
    return subreddit != null && subreddit.isOver18() && !userOver18;
  }

  private void loadHeaderImage() {
    mMainView.loadImageIntoDrawerHeader(
        mSubredditInfo == null ? null : mSubredditInfo.getHeaderImageUrl());
  }
}
