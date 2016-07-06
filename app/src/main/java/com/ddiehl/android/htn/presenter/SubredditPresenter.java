package com.ddiehl.android.htn.presenter;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.RedditNavigationView;
import com.ddiehl.android.htn.view.SubredditView;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
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
    mRedditService.loadLinks(mSubredditView.getSubreddit(), mSubredditView.getSort(), mSubredditView.getTimespan(),
        append ? null : mPrevPageListingId,
        append ? mNextPageListingId : null)
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
            onListingsLoaded(append),
            e -> {
              String message = mContext.getString(R.string.error_get_links);
              mMainView.showError(e, message);
            });
  }

  private Action1<Subreddit> onSubredditInfoLoaded() {
    return info -> {
      mSubredditInfo = info;

      boolean over18 = mSettingsManager.getOver18();
      if (shouldShowNsfwDialog(mSubredditInfo, over18)) {
        mSubredditView.showNsfwWarningDialog();
      } else {
        if (mSubredditInfo != null) requestNextData();
        else mMainView.showToast(R.string.error_private_subreddit);
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
