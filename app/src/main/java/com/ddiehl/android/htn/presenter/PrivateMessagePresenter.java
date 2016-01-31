package com.ddiehl.android.htn.presenter;

import android.os.Handler;

import com.ddiehl.android.htn.view.ListingsView;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.PrivateMessageView;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.ListingResponse;
import com.ddiehl.reddit.listings.PrivateMessage;

import java.util.List;

public class PrivateMessagePresenter extends BaseListingsPresenter implements MessagePresenter {
  private PrivateMessageView mMessageView;
  private List<Listing> mMessageList;

  public PrivateMessagePresenter(
      MainView main, ListingsView listingsView, PrivateMessageView messageView,
      List<Listing> messages) {
    super(main, listingsView, null, null, null, messageView, null, null, null, null, null);
    mMessageView = messageView;
    mMessageList = messages;
  }

  @Override
  void requestData() {
    // We already have the data, just display it
    ListingResponse response = new ListingResponse(mMessageList);
    super.onListingsLoaded().call(response);
    // Scroll to bottom so user sees the latest message
    new Handler().post(mListingsView::scrollToBottom);
    mMessageView.showSubject(
        ((PrivateMessage) mMessageList.get(0))
            .getSubject());
  }
}
