package com.ddiehl.android.htn.presenter;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.RedditNavigationView;
import com.ddiehl.android.htn.view.SubredditView;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rxreddit.model.ListingResponse;
import rxreddit.model.Subreddit;
import rxreddit.model.UserIdentity;

public class SubredditPresenter extends BaseListingsPresenter implements LinkPresenter {

  private SubredditView mSubredditView;

  public SubredditPresenter(MainView main, RedditNavigationView navigationView, SubredditView view) {
    super(main, navigationView, view, view, null, null, null);
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
      UserIdentity user = getAuthorizedUser();
      if (shouldShowNsfwDialog(mSubredditInfo, user)) {
        mSubredditView.showNsfwWarningDialog();
      } else {
        if (mSubredditInfo != null) requestNextData();
        else mMainView.showToast(R.string.error_private_subreddit);
      }
      loadHeaderImage();
    };
  }

  private boolean shouldShowNsfwDialog(Subreddit info, UserIdentity user) {
    return (info != null && info.isOver18())
        && (user == null || !user.isOver18());
  }

  @Override
  protected Action1<ListingResponse> onListingsLoaded(boolean append) {
    return (response) -> {
      super.onListingsLoaded(append).call(response);
      updateTitle();
    };
  }

  private void updateTitle() {
    if (mSubredditView.getSubreddit() == null) {
      mMainView.setTitle(R.string.front_page_title);
    } else {
      mMainView.setTitle(
          String.format(mContext.getString(R.string.link_subreddit), mSubredditView.getSubreddit()));
    }
  }

  private void loadHeaderImage() {
    mMainView.loadImageIntoDrawerHeader(
        mSubredditInfo == null ? null : mSubredditInfo.getHeaderImageUrl());
  }
}
