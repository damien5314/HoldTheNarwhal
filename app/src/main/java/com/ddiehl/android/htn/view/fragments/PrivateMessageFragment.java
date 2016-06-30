package com.ddiehl.android.htn.view.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.TextView;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.PrivateMessagePresenter;
import com.ddiehl.android.htn.view.PrivateMessageView;
import com.ddiehl.android.htn.view.adapters.ListingsAdapter;
import com.ddiehl.android.htn.view.adapters.PrivateMessageAdapter;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import rxreddit.model.Listing;
import rxreddit.model.PrivateMessage;

public class PrivateMessageFragment extends BaseListingsFragment
    implements PrivateMessageView {

  public static final String TAG = PrivateMessageFragment.class.getSimpleName();

  private static final String ARG_MESSAGES = "arg_messages";

  private PrivateMessagePresenter mPrivateMessagePresenter;

  @Inject protected Gson mGson;
  @Bind(R.id.conversation_subject) TextView mConversationSubject;

  public static PrivateMessageFragment newInstance(Gson gson, List<? extends Listing> messages) {
    PrivateMessageFragment fragment = new PrivateMessageFragment();
    Bundle args = new Bundle();
    args.putString(ARG_MESSAGES, gson.toJson(messages));
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    HoldTheNarwhal.getApplicationComponent().inject(this);
    List<Listing> messages = null;
    if (getArguments() != null) {
      String json = getArguments().getString(ARG_MESSAGES);
      messages = Arrays.asList(mGson.fromJson(json, PrivateMessage[].class));
    }
    mPrivateMessagePresenter = new PrivateMessagePresenter(mMainView, this, this, messages);
    mMessagePresenter = mPrivateMessagePresenter;
    mListingsPresenter = mPrivateMessagePresenter;
    mCallbacks = mPrivateMessagePresenter;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);

    menu.findItem(R.id.action_change_timespan)
        .setVisible(false);
    menu.findItem(R.id.action_refresh)
        .setVisible(false);
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
}
