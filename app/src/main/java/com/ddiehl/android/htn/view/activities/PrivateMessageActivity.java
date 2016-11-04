package com.ddiehl.android.htn.view.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.view.fragments.PrivateMessageFragment;
import com.ddiehl.android.htn.view.fragments.PrivateMessageFragmentBuilder;
import com.google.gson.Gson;

import java.util.List;

import javax.inject.Inject;

import rxreddit.model.PrivateMessage;

public class PrivateMessageActivity extends FragmentActivityCompat {

    private static final String EXTRA_MESSAGES = "EXTRA_MESSAGES";

    @Inject protected Gson mGson;

    public static Intent getIntent(Context context, Gson gson, List<PrivateMessage> messages) {
        Intent intent = new Intent(context, PrivateMessageActivity.class);
        String json = gson.toJson(messages);
        intent.putExtra(EXTRA_MESSAGES, json); // lol
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HoldTheNarwhal.getApplicationComponent().inject(this);
        if (getIntent().getExtras() == null) {
            throw new RuntimeException("no extras passed to PrivateMessageActivity");
        }
        showTabs(false);
    }

    private String getJson() {
        return getIntent().getStringExtra(EXTRA_MESSAGES);
    }

    @Override
    Fragment getFragment() {
        return new PrivateMessageFragmentBuilder(getJson())
                .build();
    }

    @Override
    String getFragmentTag() {
        return PrivateMessageFragment.TAG;
    }
}
