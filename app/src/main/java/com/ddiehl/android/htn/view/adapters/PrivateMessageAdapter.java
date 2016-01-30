package com.ddiehl.android.htn.view.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.ListingsPresenter;
import com.ddiehl.android.htn.presenter.MessagePresenter;
import com.ddiehl.android.htn.view.viewholders.ListingsMessageViewHolder;

public class PrivateMessageAdapter extends ListingsAdapter {
  private static final int TYPE_MESSAGE = 0x1;

  public PrivateMessageAdapter(
      ListingsPresenter listingsPresenter, MessagePresenter messagePresenter) {
    super(listingsPresenter, null, null, messagePresenter);
  }

  @Override
  public int getItemViewType(int position) {
    return TYPE_MESSAGE;
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.listings_message_nocard, parent, false);
    return new ListingsMessageViewHolder(view, mMessagePresenter);
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    super.onBindViewHolder(holder, position);
  }

  @Override
  protected void getSettings() {
    /* no-op */
  }
}
