package com.ddiehl.android.htn.view.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.PrivateMessagePresenter;
import com.ddiehl.android.htn.view.PrivateMessageView;
import com.ddiehl.android.htn.view.adapters.ListingsAdapter;
import com.ddiehl.android.htn.view.adapters.PrivateMessageAdapter;
import com.ddiehl.reddit.listings.Listing;

import org.parceler.Parcels;

import java.util.List;

import butterknife.Bind;

public class PrivateMessageFragment extends BaseListingsFragment implements PrivateMessageView {
  private static final String ARG_MESSAGES = "arg_messages";

  private PrivateMessagePresenter mPrivateMessagePresenter;

  @Bind(R.id.conversation_subject)
  TextView mConversationSubject;

  public static PrivateMessageFragment newInstance(List<? extends Listing> messages) {
    PrivateMessageFragment fragment = new PrivateMessageFragment();
    Bundle args = new Bundle();
    args.putParcelable(ARG_MESSAGES, Parcels.wrap(messages));
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    List<Listing> messages = null;
    if (getArguments() != null) {
      messages = Parcels.unwrap(getArguments().getParcelable(ARG_MESSAGES));
    }
    mPrivateMessagePresenter = new PrivateMessagePresenter(mMainView, this, this, messages);
    mMessagePresenter = mPrivateMessagePresenter;
    mListingsPresenter = mPrivateMessagePresenter;
  }

  @Nullable @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View v = super.onCreateView(inflater, container, savedInstanceState);
    updateTitle();
    return v;
  }

  @Override
  public ListingsAdapter getListingsAdapter() {
    return new PrivateMessageAdapter(mListingsPresenter, mMessagePresenter);
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.listings_fragment_private_message;
  }

  @Override
  public void showSubject(@NonNull String subject) {
    mConversationSubject.setText(subject);
  }

  @Override
  public void updateTitle() {

  }
}
