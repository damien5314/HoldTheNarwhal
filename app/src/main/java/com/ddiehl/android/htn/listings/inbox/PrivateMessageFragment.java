package com.ddiehl.android.htn.listings.inbox;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.listings.report.ReportActivity;
import com.ddiehl.android.htn.view.BaseFragment;
import com.google.gson.Gson;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import rxreddit.model.Listing;
import rxreddit.model.PrivateMessage;

@FragmentWithArgs
public class PrivateMessageFragment extends BaseFragment implements PrivateMessageView {

    public static final String TAG = PrivateMessageFragment.class.getSimpleName();

    private static final int REQUEST_REPORT_MESSAGE = 1000;

    @Arg String json;

    @Inject Gson gson;

    @BindView(R.id.coordinator_layout) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;

    PrivateMessageAdapter adapter;

    @Override
    protected int getLayoutResId() {
        return R.layout.listings_fragment_private_message;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HoldTheNarwhal.getApplicationComponent().inject(this);
        FragmentArgs.inject(this);
        adapter = new PrivateMessageAdapter();
    }

    @NotNull @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        View view = super.onCreateView(inflater, container, state);

        // Configure RecyclerView
        recyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false
        );
        recyclerView.setLayoutManager(layoutManager);

        // Add passed messages to adapter
        List<PrivateMessage> messages = Arrays.asList(
                gson.fromJson(json, PrivateMessage[].class)
        );
        adapter.getMessages().addAll(messages);
        adapter.notifyDataSetChanged();

        // Set text for subject view
        String subject = messages.get(0).getSubject();
        getActivity().setTitle(subject);

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

    private void scrollToBottom() {
        new Handler().post(() ->
                recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1)
        );
    }

    @NotNull @Override
    protected View getChromeView() {
        return coordinatorLayout;
    }

    @Override
    public void openReportView(@NotNull PrivateMessage message) {
        Intent intent = ReportActivity.getIntent(getContext(), message.getFullName(), null);
        startActivityForResult(intent, REQUEST_REPORT_MESSAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_REPORT_MESSAGE:
                if (resultCode == ReportActivity.RESULT_REPORT_SUCCESS) {
                    // Passing null because we don't have access to the selected listing in this view
                    // But actually we're not showing context menus here yet, so it's ok
                    showReportSuccessToast(null);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    void showReportSuccessToast(@NotNull Listing listing) {
        Snackbar.make(getChromeView(), R.string.report_successful, Snackbar.LENGTH_LONG)
                .show();
    }
}
