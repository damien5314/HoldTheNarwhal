package com.ddiehl.android.htn.listings.inbox;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.BaseActivity;
import com.google.gson.Gson;

import javax.inject.Inject;

public class PrivateMessageActivity extends BaseActivity {

    private static final String EXTRA_MESSAGES = "EXTRA_MESSAGES";

    @Inject protected Gson mGson;

    public static Intent getIntent(Context context, String messagesJson) {
        Intent intent = new Intent(context, PrivateMessageActivity.class);
        intent.putExtra(EXTRA_MESSAGES, messagesJson); // lol
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

    @Override
    protected void onStart() {
        super.onStart();
        if (getSupportFragmentManager().findFragmentByTag(getFragmentTag()) == null) {
            final Fragment fragment = getFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment, getFragmentTag())
                    .commit();
        }
    }

    private String getJson() {
        return getIntent().getStringExtra(EXTRA_MESSAGES);
    }

    private Fragment getFragment() {
        return new PrivateMessageFragmentBuilder(getJson())
                .build();
    }

    private String getFragmentTag() {
        return PrivateMessageFragment.TAG;
    }
}
