package com.ddiehl.android.htn.listings.inbox;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.ddiehl.android.htn.view.FragmentActivityCompat2;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import rxreddit.model.PrivateMessage;

public class PrivateMessageActivity extends FragmentActivityCompat2 {

    private static final String EXTRA_MESSAGES = "EXTRA_MESSAGES";

    @Inject protected Gson mGson;

    public static Intent getIntent(Context context, Gson gson, List<PrivateMessage> messages) {
        Intent intent = new Intent(context, PrivateMessageActivity.class);
        String json = gson.toJson(messages);
        intent.putExtra(EXTRA_MESSAGES, json); // lol
        return intent;
    }

    @Override
    protected boolean hasNavigationDrawer() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getExtras() == null) {
            throw new RuntimeException("no extras passed to PrivateMessageActivity");
        }
        showTabs(false);
    }

    private String getJson() {
        return getIntent().getStringExtra(EXTRA_MESSAGES);
    }

    @NotNull @Override
    protected Fragment getFragment() {
        return new PrivateMessageFragmentBuilder(getJson())
                .build();
    }

    @NotNull @Override
    protected String getFragmentTag() {
        return PrivateMessageFragment.TAG;
    }
}
