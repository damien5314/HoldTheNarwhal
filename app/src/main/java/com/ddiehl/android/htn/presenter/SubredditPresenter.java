package com.ddiehl.android.htn.presenter;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.ListingsView;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.Link;
import com.ddiehl.reddit.listings.ListingResponse;
import com.ddiehl.reddit.listings.Subreddit;

import rx.functions.Action0;
import rx.functions.Action1;

public class SubredditPresenter extends AbsListingsPresenter {
  public SubredditPresenter(
      MainView main, ListingsView view, String subreddit, String sort, String timespan) {
    super(main, view, null, null, subreddit, sort, timespan);
    mMainView.loadImageIntoDrawerHeader(null);
  }

  @Override
  public void onResume() {
    super.onResume();
    loadHeaderImage();
  }

  @Override
  public void onPause() {
    super.onPause();
    mMainView.loadImageIntoDrawerHeader(null);
  }

  @Override
  public void requestData() {
    if (mSubreddit == null || mSubreddit.equals("all") || mSubredditInfo != null) {
      mAnalytics.logLoadSubreddit(mSubreddit, mSort, mTimespan);
      mRedditService.loadLinks(mSubreddit, mSort, mTimespan, mNextPageListingId)
          .doOnTerminate(onGetDataCompleted())
          .subscribe(onListingsLoaded(),
              e -> mMainView.showError(e, R.string.error_get_links));
    } else {
      mListingsRequested = false;
      mRedditService.getSubredditInfo(mSubreddit)
          .doOnTerminate(onGetDataCompleted())
          .subscribe(onSubredditInfoLoaded(),
              e -> mMainView.showError(e, R.string.error_get_subreddit_info));
    }
  }

  private Action0 onGetDataCompleted() {
    return () -> {
      mMainView.dismissSpinner();
      mListingsRequested = false;
    };
  }

  private Action1<Subreddit> onSubredditInfoLoaded() {
    return info -> {
      mSubredditInfo = info;
      UserIdentity user = getAuthorizedUser();
      if (shouldShowNsfwDialog(mSubredditInfo, user)) {
        mMainView.showNsfwWarningDialog();
      } else {
        // FIXME Need to check for this while 4xx responses are coming back as successful
        if (mSubredditInfo != null) requestData();
      }
      loadHeaderImage();
    };
  }

  private boolean shouldShowNsfwDialog(Subreddit info, UserIdentity user) {
    return (info != null && info.isOver18())
        && (user == null || !user.isOver18());
  }

  @Override
  protected Action1<ListingResponse> onListingsLoaded() {
    return (response) -> {
      super.onListingsLoaded().call(response);
      if (mSubreddit != null && mSubreddit.equals("random")) {
        mSubreddit = ((Link) mListings.get(0)).getSubreddit();
        mListingsView.updateTitle();
      }
    };
  }

  private void loadHeaderImage() {
    if (mSubredditInfo != null) {
      mMainView.loadImageIntoDrawerHeader(mSubredditInfo.getHeaderImageUrl());
    }
  }
}
