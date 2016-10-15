package com.ddiehl.android.htn.view.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.PrivateMessageView;
import com.ddiehl.android.htn.view.adapters.PrivateMessageAdapter;
import com.google.gson.Gson;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rxreddit.model.PrivateMessage;

@FragmentWithArgs
public class PrivateMessageFragment extends BaseFragment
        implements PrivateMessageView {

    public static final String TAG = PrivateMessageFragment.class.getSimpleName();

    @Arg String mJson;

    @Inject protected Gson mGson;

    @BindView(R.id.coordinator_layout) protected CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.conversation_subject) protected TextView mConversationSubject;
    @BindView(R.id.recycler_view) protected RecyclerView mRecyclerView;

    private PrivateMessageAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HoldTheNarwhal.getApplicationComponent().inject(this);
        FragmentArgs.inject(this);
        mAdapter = new PrivateMessageAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.listings_fragment_private_message, container, false);
        ButterKnife.bind(this, view);

        // Configure RecyclerView
        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);

        // Add passed messages to adapter
        List<PrivateMessage> messages = Arrays.asList(
                mGson.fromJson(mJson, PrivateMessage[].class));
        mAdapter.getMessages().addAll(messages);
        mAdapter.notifyDataSetChanged();
        String subject = messages.get(0).getSubject();
        mConversationSubject.setText(subject);

        // Scroll to bottom so user sees the latest message
        scrollToBottom();

        return view;
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
    public void showMessageContextMenu(ContextMenu menu, View v, PrivateMessage privateMessage) {

    }

    @Override
    public void showSubject(@NonNull String subject) {
        mConversationSubject.setText(subject);
    }

    private void scrollToBottom() {
        new Handler().post(() ->
                mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1)
        );
    }

    @Override
    protected View getChromeView() {
        return mCoordinatorLayout;
    }
}
