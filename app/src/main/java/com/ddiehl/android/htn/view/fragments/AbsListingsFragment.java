/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.ddiehl.android.htn.Analytics;
import com.ddiehl.android.htn.BusProvider;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.ListingsPresenter;
import com.ddiehl.android.htn.view.ListingsView;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.activities.MainActivity;
import com.ddiehl.android.htn.view.adapters.ListingsAdapter;
import com.ddiehl.android.htn.view.dialogs.ChooseLinkSortDialog;
import com.ddiehl.android.htn.view.dialogs.ChooseTimespanDialog;
import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.Link;
import com.squareup.otto.Bus;

import butterknife.ButterKnife;

public abstract class AbsListingsFragment extends Fragment
        implements ListingsView, SwipeRefreshLayout.OnRefreshListener {

    private static final int REQUEST_CHOOSE_SORT = 0x2;
    private static final int REQUEST_CHOOSE_TIMESPAN = 0x3;
    private static final String DIALOG_CHOOSE_SORT = "dialog_choose_sort";
    private static final String DIALOG_CHOOSE_TIMESPAN = "dialog_choose_timespan";

    Bus mBus = BusProvider.getInstance();
    Analytics mAnalytics = Analytics.getInstance();

    MainView mMainView;
    ListingsPresenter mListingsPresenter;
    ListingsAdapter mListingsAdapter;
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        mBus = BusProvider.getInstance();
        mMainView = (MainView) getActivity();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSwipeRefreshLayout = ButterKnife.findById(view, R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    void instantiateListView(View v) {
        RecyclerView rv = ButterKnife.findById(v, R.id.recycler_view);
        final LinearLayoutManager mgr = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(mgr);
        rv.clearOnScrollListeners();
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int mFirstVisibleItem, mVisibleItemCount, mTotalItemCount;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                mVisibleItemCount = mgr.getChildCount();
                mTotalItemCount = mgr.getItemCount();
                mFirstVisibleItem = mgr.findFirstVisibleItemPosition();

                if ((mVisibleItemCount + mFirstVisibleItem) >= mTotalItemCount) {
                    if (mListingsPresenter.getNextPageListingId() != null) {
                        mListingsPresenter.getMoreData();
                    }
                }
            }
        });
        rv.setAdapter(mListingsAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        mListingsPresenter.onResume();
    }

    @Override
    public void onPause() {
        mListingsPresenter.onPause();
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHOOSE_SORT:
                String sort = data.getStringExtra(ChooseLinkSortDialog.EXTRA_SORT);
                mListingsPresenter.onSortSelected(sort);
                break;
            case REQUEST_CHOOSE_TIMESPAN:
                String timespan = data.getStringExtra(ChooseTimespanDialog.EXTRA_TIMESPAN);
                mListingsPresenter.onTimespanSelected(timespan);
                break;
            case MainActivity.REQUEST_NSFW_WARNING:
                boolean result = resultCode == Activity.RESULT_OK;
                mListingsPresenter.onNsfwSelected(result);
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.links_menu, menu);

        // Disable timespan option if current sort does not support it
        String sort = mListingsPresenter.getSort();
        if (sort.equals("hot") || sort.equals("new") || sort.equals("rising")) {
            menu.findItem(R.id.action_change_timespan).setVisible(false);
        } else { // controversial, top
            menu.findItem(R.id.action_change_timespan).setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_sort:
                showSortOptionsMenu();
                mAnalytics.logOptionChangeSort();
                return true;
            case R.id.action_change_timespan:
                showTimespanOptionsMenu();
                mAnalytics.logOptionChangeTimespan();
                return true;
            case R.id.action_refresh:
                mListingsPresenter.refreshData();
                mAnalytics.logOptionRefresh();
                return true;
            case R.id.action_settings:
                ((MainActivity) getActivity()).showSettings();
                mAnalytics.logOptionSettings();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showSortOptionsMenu() {
        FragmentManager fm = getActivity().getFragmentManager();
        ChooseLinkSortDialog chooseLinkSortDialog = ChooseLinkSortDialog.newInstance(mListingsPresenter.getSort());
        chooseLinkSortDialog.setTargetFragment(this, REQUEST_CHOOSE_SORT);
        chooseLinkSortDialog.show(fm, DIALOG_CHOOSE_SORT);
    }

    @Override
    public void showTimespanOptionsMenu() {
        FragmentManager fm = getActivity().getFragmentManager();
        ChooseTimespanDialog chooseTimespanDialog = ChooseTimespanDialog.newInstance(mListingsPresenter.getTimespan());
        chooseTimespanDialog.setTargetFragment(this, REQUEST_CHOOSE_TIMESPAN);
        chooseTimespanDialog.show(fm, DIALOG_CHOOSE_TIMESPAN);
    }

    @Override
    public void onSortChanged() {
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onTimespanChanged() {
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void goBack() {
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        }
    }

    @Override
    public void showLinkContextMenu(ContextMenu menu, View v, Link link) {
        getActivity().getMenuInflater().inflate(R.menu.link_context_menu, menu);
        String title = String.format(v.getContext().getString(R.string.menu_action_link),
                link.getTitle(), link.getScore());
        menu.setHeaderTitle(title);
        menu.findItem(R.id.action_link_hide).setVisible(!link.isHidden());
        menu.findItem(R.id.action_link_unhide).setVisible(link.isHidden());
        // Set username for listing in the user profile menu item
        String username = String.format(getString(R.string.action_view_user_profile), link.getAuthor());
        menu.findItem(R.id.action_link_view_user_profile).setTitle(username);
    }

    @Override
    public void showCommentContextMenu(ContextMenu menu, View v, Comment comment) {
        getActivity().getMenuInflater().inflate(R.menu.comment_context_menu, menu);
        String title = String.format(getString(R.string.menu_action_comment),
                comment.getAuthor(), comment.getScore());
        menu.setHeaderTitle(title);
        if (comment.isArchived()) {
            menu.findItem(R.id.action_comment_report).setVisible(false);
        }
        // Set username for listing in the user profile menu item
        String username = String.format(getString(R.string.action_view_user_profile), comment.getAuthor());
        menu.findItem(R.id.action_comment_view_user_profile).setTitle(username);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_link_upvote:
                mListingsPresenter.upvoteLink();
                return true;
            case R.id.action_link_downvote:
                mListingsPresenter.downvoteLink();
                return true;
            case R.id.action_link_show_comments:
                mListingsPresenter.showCommentsForLink();
                return true;
            case R.id.action_link_save:
                mListingsPresenter.saveLink();
                return true;
            case R.id.action_link_unsave:
                mListingsPresenter.unsaveLink();
                return true;
            case R.id.action_link_share:
                mListingsPresenter.shareLink();
                return true;
            case R.id.action_link_view_user_profile:
                mListingsPresenter.openLinkUserProfile();
                return true;
            case R.id.action_link_open_in_browser:
                mListingsPresenter.openLinkInBrowser();
                return true;
            case R.id.action_link_open_comments_in_browser:
                mListingsPresenter.openCommentsInBrowser();
                return true;
            case R.id.action_link_hide:
                mListingsPresenter.hideLink();
                return true;
            case R.id.action_link_unhide:
                mListingsPresenter.unhideLink();
                return true;
            case R.id.action_link_report:
                mListingsPresenter.reportLink();
                return true;
            case R.id.action_comment_permalink:
                mListingsPresenter.openCommentPermalink();
                return true;
            case R.id.action_comment_reply:
                mListingsPresenter.openReplyView();
                return true;
            case R.id.action_comment_upvote:
                mListingsPresenter.upvoteComment();
                return true;
            case R.id.action_comment_downvote:
                mListingsPresenter.downvoteComment();
                return true;
            case R.id.action_comment_save:
                mListingsPresenter.saveComment();
                return true;
            case R.id.action_comment_unsave:
                mListingsPresenter.unsaveComment();
                return true;
            case R.id.action_comment_share:
                mListingsPresenter.shareComment();
                return true;
            case R.id.action_comment_open_in_browser:
                mListingsPresenter.openCommentInBrowser();
                return true;
            case R.id.action_comment_report:
                mListingsPresenter.reportComment();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void openShareView(@NonNull Link link) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, "http://www.reddit.com" + link.getPermalink());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public void openLinkInBrowser(@NonNull Link link) {
        Uri uri = Uri.parse(link.getUrl());
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public void openCommentsInBrowser(@NonNull Link link) {
        Uri uri = Uri.parse("http://www.reddit.com" + link.getPermalink());
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public void openLinkInWebView(@NonNull Link link) {
        mAnalytics.logOpenLink(link);
        ((MainActivity) getActivity()).showWebViewForURL(link.getUrl());
    }

    @Override
    public void showCommentsForLink(@Nullable String subreddit, @Nullable String linkId,
                                    @NonNull String commentId) {
        Fragment fragment = LinkCommentsFragment.newInstance(subreddit, linkId, commentId);
        FragmentManager fm = getActivity().getFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void showCommentThread(@Nullable String subreddit, @Nullable String linkId,
                                  @NonNull String commentId) {
        showCommentsForLink(subreddit, linkId, commentId);
    }

    @Override
    public void openReplyView(@NonNull Comment comment) {
        mMainView.showToast(R.string.implementation_pending);
    }

    @Override
    public void openShareView(@NonNull Comment comment) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, comment.getUrl());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public void openUserProfileView(@NonNull Link link) {
        ((MainView) getActivity()).showUserProfile("summary", link.getAuthor());
    }

    @Override
    public void openUserProfileView(@NonNull Comment comment) {
        ((MainView) getActivity()).showUserProfile("summary", comment.getAuthor());
    }

    @Override
    public void openCommentInBrowser(@NonNull Comment comment) {
        Uri uri = Uri.parse(comment.getUrl());
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public void listingsUpdated() {
        mListingsAdapter.notifyDataSetChanged();
//        mMainView.updateTitle();
    }

    @Override
    public void listingUpdatedAt(int position) {
        mListingsAdapter.notifyItemChanged(position);
    }

    @Override
    public void listingRemovedAt(int position) {
        mListingsAdapter.notifyItemRemoved(position);
    }

    @Override
    public void onRefresh() {
        mListingsPresenter.refreshData();
        mAnalytics.logOptionRefresh();
    }

//    @Override
//    public void showSpinner(String msg) {
//        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(true));
//    }
//
//    @Override
//    public void showSpinner(int resId) {
//        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(true));
//    }
//
//    @Override
//    public void dismissSpinner() {
//        mSwipeRefreshLayout.setRefreshing(false);
//    }
}
