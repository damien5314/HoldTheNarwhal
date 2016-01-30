package com.ddiehl.android.htn.presenter;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.LinkView;
import com.ddiehl.android.htn.view.ListingsView;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.Link;
import com.ddiehl.reddit.listings.ListingResponse;
import com.ddiehl.reddit.listings.Subreddit;

import rx.functions.Action1;

public class SubredditPresenter extends AbsListingsPresenter implements LinkPresenter {
  public SubredditPresenter(
      MainView main, ListingsView listingsView, LinkView view,
      String subreddit, String sort, String timespan) {
    super(main, listingsView, view, null, null, null, null, null, subreddit, sort, timespan);
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
    if (mSubreddit != null && !mSubreddit.equals("all") && mSubredditInfo == null) {
      mMainView.showSpinner(null);
//      mListingsRequested = false;
      mRedditService.getSubredditInfo(mSubreddit)
          .doOnTerminate(() -> {
            if (!mListingsRequested) mMainView.dismissSpinner();
            mListingsRequested = false;
          })
          .subscribe(onSubredditInfoLoaded(),
              e -> mMainView.showError(e, R.string.error_get_subreddit_info));
    } else {
      mMainView.showSpinner(null);
      mListingsRequested = true;
      mAnalytics.logLoadSubreddit(mSubreddit, mSort, mTimespan);
      mRedditService.loadLinks(mSubreddit, mSort, mTimespan, mNextPageListingId)
          .doOnTerminate(() -> {
            mMainView.dismissSpinner();
            mListingsRequested = false;
          })
          .subscribe(onListingsLoaded(),
              e -> mMainView.showError(e, R.string.error_get_links));
    }
  }

  private Action1<Subreddit> onSubredditInfoLoaded() {
    return info -> {
      mSubredditInfo = info;
      UserIdentity user = getAuthorizedUser();
      if (shouldShowNsfwDialog(mSubredditInfo, user)) {
        mMainView.showNsfwWarningDialog();
      } else {
        if (mSubredditInfo != null) requestData();
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
