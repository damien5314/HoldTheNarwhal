package com.ddiehl.android.htn.view.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.ddiehl.android.htn.view.fragments.PrivateMessageFragment;

import org.parceler.Parcels;

import java.util.List;

import rxreddit.model.PrivateMessage;

public class PrivateMessageActivity extends BaseActivity {

  private static final String EXTRA_MESSAGES = "EXTRA_MESSAGES";

  public static Intent getIntent(Context context, List<PrivateMessage> messages) {
    Intent intent = new Intent(context, PrivateMessageActivity.class);
    intent.putExtra(EXTRA_MESSAGES, Parcels.wrap(messages));
    return intent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getIntent().getExtras() == null) {
      throw new RuntimeException("no extras passed to PrivateMessageActivity");
    }
  }

  private List<PrivateMessage> getMessages() {
    return Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_MESSAGES));
  }

  @Override
  Fragment getFragment() {
    return PrivateMessageFragment.newInstance(getMessages());
  }

  @Override
  String getFragmentTag() {
    return PrivateMessageFragment.TAG;
  }
}
