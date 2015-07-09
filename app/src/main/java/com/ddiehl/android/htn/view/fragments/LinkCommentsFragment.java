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
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.htn.BusProvider;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.SettingsManager;
import com.ddiehl.android.htn.presenter.LinkCommentsPresenter;
import com.ddiehl.android.htn.presenter.LinkCommentsPresenterImpl;
import com.ddiehl.android.htn.view.LinkCommentsView;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.SettingsChangedListener;
import com.ddiehl.android.htn.view.activities.MainActivity;
import com.ddiehl.android.htn.view.adapters.LinkCommentsAdapter;
import com.ddiehl.android.htn.view.dialogs.ChooseCommentSortDialog;
import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.Link;
import com.flurry.android.FlurryAgent;
import com.squareup.otto.Bus;

import butterknife.ButterKnife;

public class LinkCommentsFragment extends AbsRedditFragment
        implements LinkCommentsView, SettingsChangedListener {

    private static final String ARG_SUBREDDIT = "subreddit";
    private static final String ARG_ARTICLE = "article";
    private static final String ARG_COMMENT_ID = "comment_id";

    private static final int REQUEST_CHOOSE_SORT = 0;
    private static final String DIALOG_CHOOSE_SORT = "dialog_choose_sort";

    private Bus mBus = BusProvider.getInstance();
    private LinkCommentsPresenter mLinkCommentsPresenter;

    private LinkCommentsAdapter mLinkCommentsAdapter;

    public LinkCommentsFragment() { /* Default constructor */ }

    public static LinkCommentsFragment newInstance(String subreddit, String article, String commentId) {
        Bundle args = new Bundle();
        args.putString(ARG_SUBREDDIT, subreddit);
        args.putString(ARG_ARTICLE, article);
        args.putString(ARG_COMMENT_ID, commentId);
        LinkCommentsFragment fragment = new LinkCommentsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        String subreddit = args.getString(ARG_SUBREDDIT);
        String articleId = args.getString(ARG_ARTICLE);
        String commentId = args.getString(ARG_COMMENT_ID);

        mLinkCommentsPresenter = new LinkCommentsPresenterImpl(getActivity(), this, subreddit, articleId, commentId);
        mLinkCommentsAdapter = new LinkCommentsAdapter(mLinkCommentsPresenter);

        updateTitle();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.link_comments_fragment, container, false);

        RecyclerView recyclerView = ButterKnife.findById(v, R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mLinkCommentsAdapter);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateTitle();
    }

    private void updateTitle() {
        Link link = mLinkCommentsPresenter.getLinkContext();
        if (link != null) {
            setTitle(link.getTitle());
        } else {
            setTitle(getString(R.string.comments_fragment_title));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mBus.register(mLinkCommentsPresenter);

        if (mLinkCommentsAdapter.getItemCount() < 2) { // Always returns at least 1
            mLinkCommentsPresenter.getComments();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mBus.unregister(mLinkCommentsPresenter);
    }

    @Override
    public void showLinkContextMenu(ContextMenu menu, View v, Link link) {
        getActivity().getMenuInflater().inflate(R.menu.link_context_menu, menu);
        String title = String.format(v.getContext().getString(R.string.menu_action_link),
                link.getTitle(), link.getScore());
        menu.setHeaderTitle(title);
        menu.findItem(R.id.action_link_show_comments).setVisible(false);
        menu.findItem(R.id.action_link_hide).setVisible(false);
        menu.findItem(R.id.action_link_unhide).setVisible(false);
        // Set username for listing in the user profile menu item
        String username = String.format(getString(R.string.action_view_user_profile), link.getAuthor());
        menu.findItem(R.id.action_link_view_user_profile).setTitle(username);
    }

    @Override
    public void linkUpdated() {
        mLinkCommentsAdapter.notifyItemChanged(0);
    }

    @Override
    public void commentsUpdated() {
        mLinkCommentsAdapter.notifyDataSetChanged();
    }

    @Override
    public void commentUpdatedAt(int position) {
        mLinkCommentsAdapter.notifyItemChanged(position + 1);
    }

    @Override
    public void commentRemovedAt(int position) {
        mLinkCommentsAdapter.notifyItemRemoved(position + 1);
    }

    @Override
    public void showCommentContextMenu(ContextMenu menu, View v, Comment comment) {
        getActivity().getMenuInflater().inflate(R.menu.comment_context_menu, menu);
        String title = String.format(v.getContext().getString(R.string.menu_action_comment),
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
    public void showCommentThread(String subreddit, String linkId, String commentId) {
        mLinkCommentsPresenter.showCommentThread(subreddit, linkId, commentId);
    }

    @Override
    public void openShareView(Comment comment) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, comment.getUrl());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public void openCommentInBrowser(Comment comment) {
        Uri uri = Uri.parse(comment.getUrl());
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public void openShareView(Link link) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, "http://www.reddit.com" + link.getPermalink());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public void openUserProfileView(Link link) {
        ((MainView) getActivity()).showUserProfile("overview", link.getAuthor());
    }

    @Override
    public void openUserProfileView(Comment comment) {
        ((MainView) getActivity()).showUserProfile("overview", comment.getAuthor());
    }

    @Override
    public void openLinkInBrowser(Link link) {
        Uri uri = Uri.parse(link.getUrl());
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public void openCommentsInBrowser(Link link) {
        Uri uri = Uri.parse("http://www.reddit.com" + link.getPermalink());
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_link_upvote:
                mLinkCommentsPresenter.upvoteLink();
                return true;
            case R.id.action_link_downvote:
                mLinkCommentsPresenter.downvoteLink();
                return true;
            case R.id.action_link_show_comments:
                mLinkCommentsPresenter.showCommentsForLink();
                return true;
            case R.id.action_link_save:
                mLinkCommentsPresenter.saveLink();
                return true;
            case R.id.action_link_unsave:
                mLinkCommentsPresenter.unsaveLink();
                return true;
            case R.id.action_link_share:
                mLinkCommentsPresenter.shareLink();
                return true;
            case R.id.action_link_view_user_profile:
                Link link = mLinkCommentsPresenter.getLinkContext();
                mLinkCommentsPresenter.openLinkUserProfile(link);
                return true;
            case R.id.action_link_open_in_browser:
                mLinkCommentsPresenter.openLinkInBrowser();
                return true;
            case R.id.action_link_open_comments_in_browser:
                mLinkCommentsPresenter.openCommentsInBrowser();
                return true;
            case R.id.action_link_hide:
                mLinkCommentsPresenter.hideLink();
                return true;
            case R.id.action_link_unhide:
                mLinkCommentsPresenter.unhideLink();
                return true;
            case R.id.action_link_report:
                mLinkCommentsPresenter.reportLink();
                return true;
            case R.id.action_comment_permalink:
                mLinkCommentsPresenter.openCommentPermalink();
                return true;
            case R.id.action_comment_reply:
                mLinkCommentsPresenter.openReplyView();
                return true;
            case R.id.action_comment_upvote:
                mLinkCommentsPresenter.upvoteComment();
                return true;
            case R.id.action_comment_downvote:
                mLinkCommentsPresenter.downvoteComment();
                return true;
            case R.id.action_comment_save:
                mLinkCommentsPresenter.saveComment();
                return true;
            case R.id.action_comment_unsave:
                mLinkCommentsPresenter.unsaveComment();
                return true;
            case R.id.action_comment_share:
                mLinkCommentsPresenter.shareComment();
                return true;
            case R.id.action_comment_view_user_profile:
                mLinkCommentsPresenter.openCommentUserProfile();
                return true;
            case R.id.action_comment_open_in_browser:
                mLinkCommentsPresenter.openCommentInBrowser();
                return true;
            case R.id.action_comment_report:
                mLinkCommentsPresenter.reportComment();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHOOSE_SORT:
                if (resultCode == Activity.RESULT_OK) {
                    String sort = data.getStringExtra(ChooseCommentSortDialog.EXTRA_SORT);
                    FlurryAgent.logEvent("option - change sort - " + sort);
                    mLinkCommentsPresenter.updateSort(sort);
                }
                getActivity().invalidateOptionsMenu();
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.comments_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_sort:
                showChooseCommentSortDialog();
                FlurryAgent.logEvent("option - change sort");
                return true;
            case R.id.action_refresh:
                mLinkCommentsPresenter.getComments();
                FlurryAgent.logEvent("option - refresh");
                return true;
            case R.id.action_settings:
                ((MainActivity) getActivity()).showSettings();
                FlurryAgent.logEvent("option - settings");
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showChooseCommentSortDialog() {
        FragmentManager fm = getActivity().getFragmentManager();
        String currentSort = SettingsManager.getInstance(getActivity()).getCommentSort();
        ChooseCommentSortDialog chooseCommentSortDialog = ChooseCommentSortDialog.newInstance(currentSort);
        chooseCommentSortDialog.setTargetFragment(this, REQUEST_CHOOSE_SORT);
        chooseCommentSortDialog.show(fm, DIALOG_CHOOSE_SORT);
    }

    @Override
    public void openLinkInWebView(Link link) {
        ((MainActivity) getActivity()).showWebViewForURL(link.getUrl());
    }

    @Override
    public void showCommentsForLink(String subreddit, String linkId, String commentId) {
        Fragment f = LinkCommentsFragment.newInstance(subreddit, linkId, commentId);
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, f)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void openReplyView(Comment comment) {
        showToast(R.string.implementation_pending);
    }

    @Override
    public void onSettingsChanged() {
        mLinkCommentsPresenter.updateSort();
    }
}
