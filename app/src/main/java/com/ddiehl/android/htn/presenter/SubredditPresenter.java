package com.ddiehl.android.htn.presenter;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.LinkView;
import com.ddiehl.android.htn.view.ListingsView;
import com.ddiehl.android.htn.view.MainView;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rxreddit.model.Link;
import rxreddit.model.ListingResponse;
import rxreddit.model.Subreddit;
import rxreddit.model.UserIdentity;

public class SubredditPresenter extends BaseListingsPresenter implements LinkPresenter {

  public SubredditPresenter(
      MainView main, ListingsView listingsView, LinkView view,
      String subreddit, String sort, String timespan) {
    super(main, listingsView, view, null, null, null, null, null, subreddit, sort, timespan);
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
    if (mSubreddit != null
        && !mSubreddit.equals("all")
        && !mSubreddit.equals("random")
        && mSubredditInfo == null) {
      getSubredditInfo();
    } else {
      getSubredditLinks(false);
    }
  }

  @Override
  public void requestNextData() {
    if (mSubreddit != null
        && !mSubreddit.equals("all")
        && !mSubreddit.equals("random")
        && mSubredditInfo == null) {
      getSubredditInfo();
    } else {
      getSubredditLinks(true);
    }
  }

  private void getSubredditInfo() {
    mRedditService.getSubredditInfo(mSubreddit)
        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe(() -> mMainView.showSpinner(null))
        .doOnTerminate(() -> {
          mMainView.dismissSpinner();
          mNextRequested = false;
        })
        .subscribe(onSubredditInfoLoaded(),
            e -> mMainView.showError(e, R.string.error_get_subreddit_info));
  }

  private void getSubredditLinks(boolean append) {
    mAnalytics.logLoadSubreddit(mSubreddit, mSort, mTimespan);
    mRedditService.loadLinks(mSubreddit, mSort, mTimespan,
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
        .subscribe(onListingsLoaded(append),
            e -> mMainView.showError(e, R.string.error_get_links));
  }

  private Action1<Subreddit> onSubredditInfoLoaded() {
    return info -> {
      mSubredditInfo = info;
      UserIdentity user = getAuthorizedUser();
      if (shouldShowNsfwDialog(mSubredditInfo, user)) {
        mMainView.showNsfwWarningDialog();
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
      if ("random".equals(mSubreddit)) {
        mSubreddit = ((Link) mListings.get(0)).getSubreddit();
      }
      updateTitle();
    };
  }

  private void updateTitle() {
    if (mSubreddit == null) mMainView.setTitle(R.string.front_page_title);
    else mMainView.setTitle(
        String.format(mContext.getString(R.string.link_subreddit), mSubreddit));
  }

  private void loadHeaderImage() {
    mMainView.loadImageIntoDrawerHeader(
        mSubredditInfo == null ? null : mSubredditInfo.getHeaderImageUrl());
  }
}
