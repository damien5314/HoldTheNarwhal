package com.ddiehl.android.simpleredditreader.view.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.presenter.CommentsPresenter;
import com.ddiehl.android.simpleredditreader.presenter.CommentsPresenterImpl;
import com.ddiehl.android.simpleredditreader.presenter.LinksPresenter;
import com.ddiehl.android.simpleredditreader.presenter.LinksPresenterImpl;
import com.ddiehl.android.simpleredditreader.view.CommentsView;
import com.ddiehl.android.simpleredditreader.view.LinksView;
import com.ddiehl.android.simpleredditreader.view.MainView;
import com.ddiehl.android.simpleredditreader.view.activities.MainActivity;
import com.ddiehl.android.simpleredditreader.view.adapters.LinkCommentsAdapter;
import com.ddiehl.android.simpleredditreader.view.dialogs.ChooseCommentSortDialog;
import com.ddiehl.android.simpleredditreader.view.dialogs.ChooseLinkSortDialog;
import com.ddiehl.reddit.listings.RedditComment;
import com.ddiehl.reddit.listings.RedditLink;
import com.squareup.otto.Bus;

public class LinkCommentsFragment extends Fragment implements LinksView, CommentsView {
    private static final String TAG = LinkCommentsFragment.class.getSimpleName();

    private static final String ARG_SUBREDDIT = "subreddit";
    private static final String ARG_ARTICLE = "article";

    private static final int REQUEST_CHOOSE_SORT = 0;
    private static final String DIALOG_CHOOSE_SORT = "dialog_choose_sort";

    private Bus mBus;
    private LinksPresenter mLinksPresenter;
    private CommentsPresenter mCommentsPresenter;

    private LinkCommentsAdapter mLinkCommentsAdapter;

    public LinkCommentsFragment() { /* Default constructor */ }

    public static LinkCommentsFragment newInstance(String subreddit, String article) {
        Bundle args = new Bundle();
        args.putString(ARG_SUBREDDIT, subreddit);
        args.putString(ARG_ARTICLE, article);
        LinkCommentsFragment fragment = new LinkCommentsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        setTitle(getString(R.string.comments_fragment_default_title));

        Bundle args = getArguments();
        String subreddit = args.getString(ARG_SUBREDDIT);
        String articleId = args.getString(ARG_ARTICLE);

        mBus = BusProvider.getInstance();
        mLinksPresenter = new LinksPresenterImpl(getActivity(), this, subreddit);
        mCommentsPresenter = new CommentsPresenterImpl(getActivity(), this, subreddit, articleId);

        mLinkCommentsAdapter = new LinkCommentsAdapter(mLinksPresenter, mCommentsPresenter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.comments_fragment, container, false);

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mLinkCommentsAdapter);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mBus.register(mLinksPresenter);
        mBus.register(mCommentsPresenter);

        if (mLinkCommentsAdapter.getItemCount() < 2) { // Always returns at least 1
            mCommentsPresenter.getComments();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mBus.unregister(mLinksPresenter);
        mBus.unregister(mCommentsPresenter);
    }

    @Override
    public void showLinkContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.link_context_menu, menu);
        menu.findItem(R.id.action_link_show_comments).setVisible(false);
        menu.findItem(R.id.action_link_hide).setVisible(false);
    }

    @Override
    public void showCommentContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.comment_context_menu, menu);
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
                mLinksPresenter.upvote();
                return true;
            case R.id.action_link_downvote:
                mLinksPresenter.downvote();
                return true;
            case R.id.action_link_show_comments:
                mLinksPresenter.showCommentsForLink();
                return true;
            case R.id.action_link_save:
                mLinksPresenter.saveLink();
                return true;
            case R.id.action_link_unsave:
                mLinksPresenter.unsaveLink();
                return true;
            case R.id.action_link_share:
                mLinksPresenter.shareLink();
                return true;
            case R.id.action_link_open_in_browser:
                mLinksPresenter.openLinkInBrowser();
                return true;
            case R.id.action_link_open_comments_in_browser:
                mLinksPresenter.openCommentsInBrowser();
                return true;
            case R.id.action_link_hide:
                mLinksPresenter.hideLink();
                return true;
            case R.id.action_link_report:
                mLinksPresenter.reportLink();
                return true;
            case R.id.action_comment_reply:
                mCommentsPresenter.openReplyView();
                return true;
            case R.id.action_comment_upvote:
                mCommentsPresenter.upvote();
                return true;
            case R.id.action_comment_downvote:
                mCommentsPresenter.downvote();
                return true;
            case R.id.action_comment_save:
                mCommentsPresenter.saveComment();
                return true;
            case R.id.action_comment_unsave:
                mCommentsPresenter.unsaveComment();
                return true;
            case R.id.action_comment_share:
                mCommentsPresenter.shareComment();
                return true;
            case R.id.action_comment_open_in_browser:
                mCommentsPresenter.openCommentInBrowser();
                return true;
            case R.id.action_comment_hide:
                mCommentsPresenter.hideComment();
                return true;
            case R.id.action_comment_report:
                mCommentsPresenter.reportComment();
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
                    String sort = data.getStringExtra(ChooseLinkSortDialog.EXTRA_SORT);
                    mCommentsPresenter.updateSort(sort);
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
                mCommentsPresenter.getComments();
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showChooseCommentSortDialog() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        ChooseCommentSortDialog chooseCommentSortDialog = ChooseCommentSortDialog.newInstance();
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
    public RecyclerView.Adapter<RecyclerView.ViewHolder> getListAdapter() {
        return mLinkCommentsAdapter;
    }

    @Override
    public void setTitle(String title) {
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
}
