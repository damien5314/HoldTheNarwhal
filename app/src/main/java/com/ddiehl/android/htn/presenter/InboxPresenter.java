package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.CommentView;
import com.ddiehl.android.htn.view.InboxView;
import com.ddiehl.android.htn.view.LinkView;
import com.ddiehl.android.htn.view.ListingsView;
import com.ddiehl.android.htn.view.MainView;

public class InboxPresenter extends BaseListingsPresenter
    implements LinkPresenter, CommentPresenter, MessagePresenter {
  private InboxView mInboxView;
  private String mShow = null;

  public InboxPresenter(
      @NonNull MainView main, @NonNull ListingsView listingsView,
      @NonNull LinkView linkView, @NonNull CommentView commentView,
      @NonNull InboxView inboxView, @Nullable String show) {
    super(main, listingsView, linkView, commentView, null, inboxView, null, null, null, null, null);
    mInboxView = inboxView;
    mShow = show;
  }

  @Override
  public void onResume() {
    super.onResume();
    mInboxView.selectTab(mShow);
  }

  @Override
  void requestData() {
    mMainView.showSpinner(null);
    mListingsRequested = true;

    // TODO Analytics
    mLog.d("Data requested for tab: " + mShow);

    mRedditService.getInbox(mShow, mNextPageListingId)
        .doOnTerminate(() -> {
          mMainView.dismissSpinner();
          mListingsRequested = false;
        })
        .subscribe(onListingsLoaded(),
            e -> mMainView.showError(e, R.string.error_get_inbox));
  }

  public void requestData(String show) {
    mShow = show;
    refreshData();
  }
}