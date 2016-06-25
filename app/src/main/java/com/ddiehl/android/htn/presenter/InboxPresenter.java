package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.CommentView;
import com.ddiehl.android.htn.view.InboxView;
import com.ddiehl.android.htn.view.LinkView;
import com.ddiehl.android.htn.view.ListingsView;
import com.ddiehl.android.htn.view.MainView;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rxreddit.model.Listing;
import rxreddit.model.PrivateMessage;

public class InboxPresenter extends BaseListingsPresenter
    implements LinkPresenter, CommentPresenter, MessagePresenter {

  private InboxView mInboxView;

  public InboxPresenter(
      @NonNull MainView main, @NonNull ListingsView listingsView,
      @NonNull LinkView linkView, @NonNull CommentView commentView,
      @NonNull InboxView inboxView, @Nullable String show) {
    super(main, listingsView, linkView, commentView, null, inboxView, show, null, null, null, null);
    mInboxView = inboxView;
    mShow = show;
  }

  @Override
  public void onResume() {
    super.onResume();
    mInboxView.selectTab(mShow);
  }

  @Override
  void requestPreviousData() {
    requestData(false);
  }

  @Override
  void requestNextData() {
    requestData(true);
  }

  private void requestData(boolean append) {
    // TODO Analytics
    mRedditService.getInbox(mShow,
        append ? null : mPrevPageListingId,
        append ? mNextPageListingId : null)
        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe(() -> {
          mMainView.showSpinner(null);
          mNextRequested = true;
        })
        .doOnTerminate(() -> {
          mMainView.dismissSpinner();
          mNextRequested = false;
        })
        .subscribe(onListingsLoaded(append),
            e -> mMainView.showError(e, R.string.error_get_inbox));
  }

  public void requestData(String show) {
    mShow = show;
    refreshData();
  }

  public void onMarkMessagesRead() {
    mRedditService.markAllMessagesRead()
        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            _void -> {
              for (Listing listing : mListings) {
                if (listing instanceof PrivateMessage) {
                  ((PrivateMessage) listing).markUnread(false);
                }
              }
              mListingsView.notifyDataSetChanged();
            },
            error -> mMainView.showError(error, R.string.error_xxx));
  }
}
