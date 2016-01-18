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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.analytics.Analytics;
import com.ddiehl.android.htn.presenter.ListingsPresenter;
import com.ddiehl.android.htn.view.ListingsView;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.activities.MainActivity;
import com.ddiehl.android.htn.view.adapters.ListingsAdapter;
import com.ddiehl.android.htn.view.dialogs.ChooseLinkSortDialog;
import com.ddiehl.android.htn.view.dialogs.ChooseTimespanDialog;
import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.Link;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.PrivateMessage;

import butterknife.Bind;
import butterknife.ButterKnife;

public abstract class AbsListingsFragment extends Fragment
    implements ListingsView, SwipeRefreshLayout.OnRefreshListener {
  private static final String LINK_BASE_URL = "http://www.reddit.com";
  private static final int REQUEST_CHOOSE_SORT = 0x2;
  private static final int REQUEST_CHOOSE_TIMESPAN = 0x3;
  private static final String DIALOG_CHOOSE_SORT = "dialog_choose_sort";
  private static final String DIALOG_CHOOSE_TIMESPAN = "dialog_choose_timespan";

  @Bind(R.id.recycler_view)
  protected RecyclerView mRecyclerView;

  protected Analytics mAnalytics = HoldTheNarwhal.getAnalytics();
  protected MainView mMainView;
  protected ListingsPresenter mListingsPresenter;
  protected ListingsAdapter mListingsAdapter;
  protected SwipeRefreshLayout mSwipeRefreshLayout;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    setHasOptionsMenu(true);
    mMainView = (MainView) getActivity();
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(getLayoutResId(), container, false);
    ButterKnife.bind(this, view);
    mListingsAdapter = getListingsAdapter();
    instantiateListView();
    return view;
  }

  protected abstract int getLayoutResId();
  protected abstract ListingsAdapter getListingsAdapter();

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mSwipeRefreshLayout = ButterKnife.findById(view, R.id.swipe_refresh_layout);
    mSwipeRefreshLayout.setOnRefreshListener(this);
  }

  private void instantiateListView() {
    final LinearLayoutManager mgr = new LinearLayoutManager(getActivity());
    mRecyclerView.setLayoutManager(mgr);
    mRecyclerView.clearOnScrollListeners();
    mRecyclerView.addOnScrollListener(
        new RecyclerView.OnScrollListener() {
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
    mRecyclerView.setAdapter(mListingsAdapter);
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
  public void onDestroyView() {
    super.onDestroyView();
    mListingsPresenter.onViewDestroyed();
    mRecyclerView.setAdapter(null);
  }

  // TODO Annotate requestCode with a @RequestCode annotation like we have in LinkCommentsFragment
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
    inflater.inflate(R.menu.listings, menu);
    // Disable timespan option if current sort does not support it
    String sort = mListingsPresenter.getSort();
    if (sort == null) {
      menu.findItem(R.id.action_change_timespan).setVisible(false);
    } else if (sort.equals("hot") || sort.equals("new") || sort.equals("rising")) {
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
        mMainView.showSettings();
        mAnalytics.logOptionSettings();
        return true;
    }
    return false;
  }

  @Override
  public void showSortOptionsMenu() {
    FragmentManager fm = getActivity().getFragmentManager();
    ChooseLinkSortDialog chooseLinkSortDialog =
        ChooseLinkSortDialog.newInstance(mListingsPresenter.getSort());
    chooseLinkSortDialog.setTargetFragment(this, REQUEST_CHOOSE_SORT);
    chooseLinkSortDialog.show(fm, DIALOG_CHOOSE_SORT);
  }

  @Override
  public void showTimespanOptionsMenu() {
    FragmentManager fm = getActivity().getFragmentManager();
    ChooseTimespanDialog chooseTimespanDialog =
        ChooseTimespanDialog.newInstance(mListingsPresenter.getTimespan());
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

  public void showLinkContextMenu(ContextMenu menu, View v, Link link) {
    getActivity().getMenuInflater().inflate(R.menu.link_context, menu);
    String title = String.format(v.getContext().getString(R.string.menu_action_link),
        link.getTitle(), link.getScore());
    menu.setHeaderTitle(title);
    menu.findItem(R.id.action_link_hide).setVisible(!link.isHidden());
    menu.findItem(R.id.action_link_unhide).setVisible(link.isHidden());
    // Set username for listing in the user profile menu item
    String username = String.format(
        getString(R.string.action_view_user_profile), link.getAuthor());
    menu.findItem(R.id.action_link_view_user_profile).setTitle(username);
  }

  public void showCommentContextMenu(ContextMenu menu, View v, Comment comment) {
    getActivity().getMenuInflater().inflate(R.menu.comment_context, menu);
    String title = String.format(getString(R.string.menu_action_comment),
        comment.getAuthor(), comment.getScore());
    menu.setHeaderTitle(title);
    // Set username for listing in the user profile menu item
    String username = String.format(
        getString(R.string.action_view_user_profile), comment.getAuthor());
    menu.findItem(R.id.action_comment_view_user_profile).setTitle(username);
  }

  public void showPrivateMessageContextMenu(
      ContextMenu menu, View v, PrivateMessage privateMessage) {
    getActivity().getMenuInflater().inflate(R.menu.message_context, menu);
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
        mListingsPresenter.replyToComment();
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

  public void openShareView(@NonNull Link link) {
    Intent i = new Intent(Intent.ACTION_SEND);
    i.setType("text/plain");
    i.putExtra(Intent.EXTRA_TEXT, LINK_BASE_URL + link.getPermalink());
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(i);
  }

  public void openLinkInBrowser(@NonNull Link link) {
    Uri uri = Uri.parse(link.getUrl());
    Intent i = new Intent(Intent.ACTION_VIEW, uri);
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(i);
  }

  public void openCommentsInBrowser(@NonNull Link link) {
    Uri uri = Uri.parse("http://www.reddit.com" + link.getPermalink());
    Intent i = new Intent(Intent.ACTION_VIEW, uri);
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(i);
  }

  public void openLinkInWebView(@NonNull Link link) {
    mAnalytics.logOpenLink(link);
    ((MainView) getActivity()).openURL(link.getUrl());
  }

  public void showCommentsForLink(
      @NonNull String subreddit, @NonNull String linkId, @Nullable String commentId) {
    mMainView.showCommentsForLink(subreddit, linkId, commentId);
  }

  public void openReplyView(@NonNull Listing listing) {
    mMainView.showToast(R.string.implementation_pending);
  }

  public void openShareView(@NonNull Comment comment) {
    Intent i = new Intent(Intent.ACTION_SEND);
    i.setType("text/plain");
    i.putExtra(Intent.EXTRA_TEXT, comment.getUrl());
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(i);
  }

  public void openUserProfileView(@NonNull Link link) {
    ((MainView) getActivity()).showUserProfile(link.getAuthor(), "summary");
  }

  public void openUserProfileView(@NonNull Comment comment) {
    ((MainView) getActivity()).showUserProfile(comment.getAuthor(), "summary");
  }

  public void openCommentInBrowser(@NonNull Comment comment) {
    Uri uri = Uri.parse(comment.getUrl());
    Intent i = new Intent(Intent.ACTION_VIEW, uri);
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(i);
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

  @Override
  public void onRefresh() {
    mSwipeRefreshLayout.setRefreshing(false);
    mListingsPresenter.refreshData();
    mAnalytics.logOptionRefresh();
  }

  @Override
  public void scrollToBottom() {
    mRecyclerView.smoothScrollToPosition(mListingsAdapter.getItemCount()-1);
  }
}
