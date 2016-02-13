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

import com.ddiehl.android.dlogger.Logger;
import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.analytics.Analytics;
import com.ddiehl.android.htn.presenter.CommentPresenter;
import com.ddiehl.android.htn.presenter.LinkPresenter;
import com.ddiehl.android.htn.presenter.ListingsPresenter;
import com.ddiehl.android.htn.presenter.MessagePresenter;
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

public abstract class BaseListingsFragment extends Fragment
    implements ListingsView, SwipeRefreshLayout.OnRefreshListener {
  private static final String LINK_BASE_URL = "http://www.reddit.com";
  private static final int REQUEST_CHOOSE_SORT = 0x2;
  private static final int REQUEST_CHOOSE_TIMESPAN = 0x3;
  private static final String DIALOG_CHOOSE_SORT = "dialog_choose_sort";
  private static final String DIALOG_CHOOSE_TIMESPAN = "dialog_choose_timespan";

  @Bind(R.id.recycler_view)
  protected RecyclerView mRecyclerView;

  protected Logger mLog = HoldTheNarwhal.getLogger();
  protected Analytics mAnalytics = HoldTheNarwhal.getAnalytics();
  protected MainView mMainView;
  protected ListingsPresenter mListingsPresenter;
  protected LinkPresenter mLinkPresenter;
  protected CommentPresenter mCommentPresenter;
  protected MessagePresenter mMessagePresenter;
  protected ListingsAdapter mListingsAdapter;
  protected SwipeRefreshLayout mSwipeRefreshLayout;
  protected Callbacks mCallbacks;

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
    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    mRecyclerView.clearOnScrollListeners();
    mRecyclerView.addOnScrollListener(mOnScrollListener);
    mRecyclerView.setAdapter(mListingsAdapter);
  }

  private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
    int mFirstVisibleItem, mVisibleItemCount, mTotalItemCount;
    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
      LinearLayoutManager mgr = (LinearLayoutManager) recyclerView.getLayoutManager();
      mVisibleItemCount = mgr.getChildCount();
      mTotalItemCount = mgr.getItemCount();
      mFirstVisibleItem = mgr.findFirstVisibleItemPosition();
//      mLog.d("First Visible: " + mFirstVisibleItem);
      if (mFirstVisibleItem == 0) {
//        mLog.d("Get PREVIOUS");
//        mListingsPresenter.getPreviousData();
        mCallbacks.onFirstItemShown();
      } else if ((mVisibleItemCount + mFirstVisibleItem) >= mTotalItemCount) {
//        mLog.d("Get NEXT");
//        mListingsPresenter.getNextData();
        mCallbacks.onLastItemShown();
      }
    }
  };

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
    String title = String.format(getString(R.string.menu_action_link),
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
        mLinkPresenter.upvoteLink();
        return true;
      case R.id.action_link_downvote:
        mLinkPresenter.downvoteLink();
        return true;
      case R.id.action_link_show_comments:
        mLinkPresenter.showCommentsForLink();
        return true;
      case R.id.action_link_save:
        mLinkPresenter.saveLink();
        return true;
      case R.id.action_link_unsave:
        mLinkPresenter.unsaveLink();
        return true;
      case R.id.action_link_share:
        mLinkPresenter.shareLink();
        return true;
      case R.id.action_link_view_user_profile:
        mLinkPresenter.openLinkUserProfile();
        return true;
      case R.id.action_link_open_in_browser:
        mLinkPresenter.openLinkInBrowser();
        return true;
      case R.id.action_link_open_comments_in_browser:
        mLinkPresenter.openCommentsInBrowser();
        return true;
      case R.id.action_link_hide:
        mLinkPresenter.hideLink();
        return true;
      case R.id.action_link_unhide:
        mLinkPresenter.unhideLink();
        return true;
      case R.id.action_link_report:
        mLinkPresenter.reportLink();
        return true;
      case R.id.action_comment_permalink:
        mCommentPresenter.openCommentPermalink();
        return true;
      case R.id.action_comment_reply:
        mCommentPresenter.replyToComment();
        return true;
      case R.id.action_comment_upvote:
        mCommentPresenter.upvoteComment();
        return true;
      case R.id.action_comment_downvote:
        mCommentPresenter.downvoteComment();
        return true;
      case R.id.action_comment_save:
        mCommentPresenter.saveComment();
        return true;
      case R.id.action_comment_unsave:
        mCommentPresenter.unsaveComment();
        return true;
      case R.id.action_comment_share:
        mCommentPresenter.shareComment();
        return true;
      case R.id.action_comment_open_in_browser:
        mCommentPresenter.openCommentInBrowser();
        return true;
      case R.id.action_comment_report:
        mCommentPresenter.reportComment();
        return true;
      case R.id.action_message_show_permalink:
        mMessagePresenter.showMessagePermalink();
        return true;
      case R.id.action_message_report:
        mMessagePresenter.reportMessage();
        return true;
      case R.id.action_message_block_user:
        mMessagePresenter.blockUser();
        return true;
      case R.id.action_message_mark_read:
        mMessagePresenter.markMessageRead();
        return true;
      case R.id.action_message_mark_unread:
        mMessagePresenter.markMessageUnread();
        return true;
      case R.id.action_message_reply:
        mMessagePresenter.replyToMessage();
        return true;
      default:
        mLog.w("No action registered to this context item");
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
    ((MainView) getActivity()).showUserProfile(link.getAuthor(), null, null);
  }

  public void openUserProfileView(@NonNull Comment comment) {
    ((MainView) getActivity()).showUserProfile(comment.getAuthor(), null, null);
  }

  public void openCommentInBrowser(@NonNull Comment comment) {
    Uri uri = Uri.parse(comment.getUrl());
    Intent i = new Intent(Intent.ACTION_VIEW, uri);
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(i);
  }

  @Override
  public void notifyDataSetChanged() {
    mListingsAdapter.notifyDataSetChanged();
  }

  @Override
  public void notifyItemChanged(int position) {
    mListingsAdapter.notifyItemChanged(position);
  }

  @Override
  public void notifyItemInserted(int position) {
    mListingsAdapter.notifyItemInserted(position);
  }

  @Override
  public void notifyItemRemoved(int position) {
    mListingsAdapter.notifyItemRemoved(position);
  }

  @Override
  public void notifyItemRangeChanged(int position, int number) {
    mListingsAdapter.notifyItemRangeChanged(position, number);
  }

  @Override
  public void notifyItemRangeInserted(int position, int number) {
    mListingsAdapter.notifyItemRangeInserted(position, number);
  }

  @Override
  public void notifyItemRangeRemoved(int position, int number) {
    mListingsAdapter.notifyItemRangeRemoved(position, number);
  }

  @Override
  public void updateTitle() {

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
