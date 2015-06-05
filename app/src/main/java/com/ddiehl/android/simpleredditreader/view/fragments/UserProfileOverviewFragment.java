package com.ddiehl.android.simpleredditreader.view.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.simpleredditreader.BusProvider;
import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.presenter.ListingPresenter;
import com.ddiehl.android.simpleredditreader.presenter.ListingPresenterImpl;
import com.ddiehl.android.simpleredditreader.view.CommentsView;
import com.ddiehl.android.simpleredditreader.view.LinksView;
import com.ddiehl.android.simpleredditreader.view.MainView;
import com.ddiehl.android.simpleredditreader.view.activities.MainActivity;
import com.ddiehl.android.simpleredditreader.view.adapters.LinkCommentsAdapter;
import com.ddiehl.android.simpleredditreader.view.dialogs.ChooseCommentSortDialog;
import com.ddiehl.reddit.listings.RedditComment;
import com.ddiehl.reddit.listings.RedditLink;
import com.squareup.otto.Bus;

public class UserProfileOverviewFragment extends UserProfileFragment
        implements LinksView, CommentsView {
    private static final String TAG = UserProfileOverviewFragment.class.getSimpleName();

    private static final int REQUEST_CHOOSE_SORT = 0;
    private static final String DIALOG_CHOOSE_SORT = "dialog_choose_sort";
    private static final String ARG_USERNAME = "arg_username";

    private Bus mBus = BusProvider.getInstance();
    private ListingPresenter mListingPresenter;
    private LinkCommentsAdapter mLinkCommentsAdapter;

    public UserProfileOverviewFragment() { }

    public static UserProfileOverviewFragment newInstance(String username) {
        UserProfileOverviewFragment f = new UserProfileOverviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USERNAME, username);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        String username = args.getString(ARG_USERNAME);
        mListingPresenter = new ListingPresenterImpl(getActivity(), this, null, username, null, null, null, null, null);
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v  = inflater.inflate(R.layout.user_profile_overview, container, false);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mBus.register(mListingPresenter);

        if (mLinkCommentsAdapter.getItemCount() < 2) { // Always returns at least 1
            mListingPresenter.getComments();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mBus.unregister(mListingPresenter);
    }

    @Override
    public void showLinkContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, RedditLink link) {
        getActivity().getMenuInflater().inflate(R.menu.link_context_menu, menu);
        String title = String.format(v.getContext().getString(R.string.menu_action_link),
                link.getTitle(), link.getScore());
        menu.setHeaderTitle(title);
        menu.findItem(R.id.action_link_show_comments).setVisible(false);
        menu.findItem(R.id.action_link_hide).setVisible(false);
        menu.findItem(R.id.action_link_unhide).setVisible(false);
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
    public void showCommentContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, RedditComment comment) {
        getActivity().getMenuInflater().inflate(R.menu.comment_context_menu, menu);
        String title = String.format(v.getContext().getString(R.string.menu_action_comment),
                comment.getAuthor(), comment.getScore());
        menu.setHeaderTitle(title);
        if (comment.isArchived()) {
            menu.findItem(R.id.action_comment_report).setVisible(false);
        }
    }

    @Override
    public void openShareView(RedditComment comment) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, comment.getUrl());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public void openCommentInBrowser(RedditComment comment) {
        Uri uri = Uri.parse(comment.getUrl());
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public void openShareView(RedditLink link) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, "http://www.reddit.com" + link.getPermalink());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public void openLinkInBrowser(RedditLink link) {
        Uri uri = Uri.parse(link.getUrl());
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public void openCommentsInBrowser(RedditLink link) {
        Uri uri = Uri.parse("http://www.reddit.com" + link.getPermalink());
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_link_upvote:
                mListingPresenter.upvote();
                return true;
            case R.id.action_link_downvote:
                mListingPresenter.downvote();
                return true;
            case R.id.action_link_show_comments:
                mListingPresenter.showCommentsForLink();
                return true;
            case R.id.action_link_save:
                mListingPresenter.saveLink();
                return true;
            case R.id.action_link_unsave:
                mListingPresenter.unsaveLink();
                return true;
            case R.id.action_link_share:
                mListingPresenter.shareLink();
                return true;
            case R.id.action_link_open_in_browser:
                mListingPresenter.openLinkInBrowser();
                return true;
            case R.id.action_link_open_comments_in_browser:
                mListingPresenter.openCommentsInBrowser();
                return true;
            case R.id.action_link_hide:
                mListingPresenter.hideLink();
                return true;
            case R.id.action_link_unhide:
                mListingPresenter.unhideLink();
                return true;
            case R.id.action_link_report:
                mListingPresenter.reportLink();
                return true;
            case R.id.action_comment_reply:
                mListingPresenter.openReplyView();
                return true;
            case R.id.action_comment_upvote:
                mListingPresenter.upvote();
                return true;
            case R.id.action_comment_downvote:
                mListingPresenter.downvote();
                return true;
            case R.id.action_comment_save:
                mListingPresenter.saveComment();
                return true;
            case R.id.action_comment_unsave:
                mListingPresenter.unsaveComment();
                return true;
            case R.id.action_comment_share:
                mListingPresenter.shareComment();
                return true;
            case R.id.action_comment_open_in_browser:
                mListingPresenter.openCommentInBrowser();
                return true;
            case R.id.action_comment_report:
                mListingPresenter.reportComment();
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
                    mListingPresenter.updateSort(sort);
                }
                getActivity().supportInvalidateOptionsMenu();
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
                return true;
            case R.id.action_refresh:
                mListingPresenter.getComments();
                return true;
            case R.id.action_settings:
                ((MainActivity) getActivity()).showSettings();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showChooseCommentSortDialog() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        ChooseCommentSortDialog chooseCommentSortDialog = ChooseCommentSortDialog.newInstance(mListingPresenter.getSort());
        chooseCommentSortDialog.setTargetFragment(this, REQUEST_CHOOSE_SORT);
        chooseCommentSortDialog.show(fm, DIALOG_CHOOSE_SORT);
    }

    @Override
    public void showSpinner(String msg) {
        ((MainView) getActivity()).showSpinner(msg);
    }

    @Override
    public void showSpinner(int resId) {
        ((MainView) getActivity()).showSpinner(resId);
    }

    @Override
    public void dismissSpinner() {
        ((MainView) getActivity()).dismissSpinner();
    }

    @Override
    public void showToast(String msg) {
        ((MainView) getActivity()).showToast(msg);
    }

    @Override
    public void showToast(int resId) {
        ((MainView) getActivity()).showToast(resId);
    }

    @Override
    public void setTitle(CharSequence title) {
        getActivity().setTitle(title);
    }

    @Override
    public void linksUpdated() {
        mLinkCommentsAdapter.notifyItemChanged(0);
    }

    @Override
    public void linkUpdatedAt(int position) {
        Log.w(TAG, "Warning: Only 1 link in this LinksView, ensure you are calling linkUpdatedAt(0)");
        mLinkCommentsAdapter.notifyItemChanged(position);
    }

    @Override
    public void linkRemovedAt(int position) {
        Log.w(TAG, "Warning: Only 1 link in this LinksView, ensure you are calling linkUpdatedAt(0)");
        mLinkCommentsAdapter.notifyItemRemoved(position);
    }

    @Override
    public void openWebViewForLink(RedditLink link) {
        ((MainActivity) getActivity()).openWebViewForURL(link.getUrl());
    }

    @Override
    public void showCommentsForLink(RedditLink link) {
        // Intentionally empty
    }

    @Override
    public void openReplyView(RedditComment comment) {
        showToast(R.string.implementation_pending);
    }
}
