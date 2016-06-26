package com.ddiehl.android.htn.presenter;

import android.os.Handler;

import com.ddiehl.android.htn.view.ListingsView;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.PrivateMessageView;

import java.util.List;

import rxreddit.model.Listing;
import rxreddit.model.ListingResponse;
import rxreddit.model.PrivateMessage;

public class PrivateMessagePresenter extends BaseListingsPresenter implements MessagePresenter {
  private final PrivateMessageView mMessageView;
  private final List<Listing> mMessageList;

  public PrivateMessagePresenter(
      MainView main, ListingsView listingsView, PrivateMessageView messageView,
      List<Listing> messages) {
    super(main, listingsView, null, null, null, messageView, null, null, null, null, null);
    mMessageView = messageView;
    mMessageList = messages;
  }

  @Override
  void requestPreviousData() {

  }

  @Override
  void requestNextData() {
    // We already have the data, just display it
    ListingResponse response = new ListingResponse(mMessageList);
    super.onListingsLoaded(true).call(response);
    // Scroll to bottom so user sees the latest message
    new Handler().post(mListingsView::scrollToBottom);
    mMessageView.showSubject(
        ((PrivateMessage) mMessageList.get(0))
            .getSubject());
  }
}
