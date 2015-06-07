package com.ddiehl.android.simpleredditreader.view.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.simpleredditreader.BusProvider;
import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.presenter.ListingsPresenter;
import com.ddiehl.android.simpleredditreader.presenter.UserProfileOverviewPresenter;
import com.ddiehl.android.simpleredditreader.view.ListingsView;
import com.ddiehl.android.simpleredditreader.view.MainView;
import com.ddiehl.android.simpleredditreader.view.activities.MainActivity;
import com.ddiehl.android.simpleredditreader.view.adapters.ListingsAdapter;
import com.ddiehl.android.simpleredditreader.view.dialogs.ChooseCommentSortDialog;
import com.ddiehl.reddit.listings.RedditComment;
import com.ddiehl.reddit.listings.RedditLink;
import com.squareup.otto.Bus;

import butterknife.ButterKnife;

public class UserProfileOverviewFragment extends AbsUserProfileFragment implements ListingsView {
    private static final String TAG = UserProfileOverviewFragment.class.getSimpleName();

    private static final int REQUEST_CHOOSE_SORT = 0;
    private static final String DIALOG_CHOOSE_SORT = "dialog_choose_sort";
    private static final String ARG_USERNAME = "arg_username";

    private Bus mBus = BusProvider.getInstance();
    private ListingsPresenter mListingsPresenter;
    private ListingsAdapter mListingsAdapter;

    private int mFirstVisibleItem, mVisibleItemCount, mTotalItemCount;
    private String mSelectedSort, mSelectedTimespan;

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
        mListingsPresenter = new UserProfileOverviewPresenter(getActivity(), this, username);
        mListingsAdapter = new ListingsAdapter(mListingsPresenter);
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v  = inflater.inflate(R.layout.listings_fragment, container, false);

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        RecyclerView rv = ButterKnife.findById(v, R.id.recycler_view);
        rv.setLayoutManager(mLayoutManager);
        rv.setAdapter(mListingsAdapter);

        rv.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                mVisibleItemCount = mLayoutManager.getChildCount();
                mTotalItemCount = mLayoutManager.getItemCount();
                mFirstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                if ((mVisibleItemCount + mFirstVisibleItem) >= mTotalItemCount) {
                    mListingsPresenter.getMoreData();
                }
            }
        });

        updateTitle();

        return v;
    }

    private void updateTitle() {
//        setTitle("");
    }

    @Override
    public void onResume() {
        super.onResume();
        mBus.register(mListingsPresenter);

        if (mListingsAdapter.getItemCount() == 0) { // Always returns at least 1
            mListingsPresenter.refreshData();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mBus.unregister(mListingsPresenter);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHOOSE_SORT:
                if (resultCode == Activity.RESULT_OK) {
                    String sort = data.getStringExtra(ChooseCommentSortDialog.EXTRA_SORT);
                    mListingsPresenter.updateSort(sort);
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
                mListingsPresenter.refreshData();
                return true;
            case R.id.action_settings:
                ((MainActivity) getActivity()).showSettings();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showChooseCommentSortDialog() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        ChooseCommentSortDialog chooseCommentSortDialog = ChooseCommentSortDialog.newInstance(mListingsPresenter.getSort());
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

    @Override
    public void listingsUpdated() {
        mListingsAdapter.notifyDataSetChanged();
    }

    @Override
    public void listingUpdatedAt(int position) {
        mListingsAdapter.notifyItemChanged(position);
    }

    @Override
    public void listingRemovedAt(int position) {
        mListingsAdapter.notifyItemRemoved(position);
    }
}
