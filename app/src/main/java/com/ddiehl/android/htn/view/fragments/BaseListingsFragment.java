package com.ddiehl.android.htn.view.fragments;

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
import com.ddiehl.android.htn.presenter.CommentPresenter;
import com.ddiehl.android.htn.presenter.LinkPresenter;
import com.ddiehl.android.htn.presenter.ListingsPresenter;
import com.ddiehl.android.htn.presenter.MessagePresenter;
import com.ddiehl.android.htn.view.ListingsView;
import com.ddiehl.android.htn.view.activities.SubredditActivity;
import com.ddiehl.android.htn.view.adapters.ListingsAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import rxreddit.model.Comment;
import rxreddit.model.Link;
import rxreddit.model.Listing;
import rxreddit.model.PrivateMessage;
import timber.log.Timber;

public abstract class BaseListingsFragment extends BaseFragment
    implements ListingsView, SwipeRefreshLayout.OnRefreshListener {

  private static final String LINK_BASE_URL = "http://www.reddit.com";

  @BindView(R.id.recycler_view) protected RecyclerView mRecyclerView;

  protected ListingsPresenter mListingsPresenter;
  protected LinkPresenter mLinkPresenter;
  protected CommentPresenter mCommentPresenter;
  protected MessagePresenter mMessagePresenter;
  protected ListingsAdapter mListingsAdapter;
  protected SwipeRefreshLayout mSwipeRefreshLayout;
  protected ListingsView.Callbacks mCallbacks;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    HoldTheNarwhal.getApplicationComponent().inject(this);
    setRetainInstance(true);
    setHasOptionsMenu(true);
  }

  @Nullable
  @Override
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
    mRecyclerView.addOnScrollListener(getOnScrollListener());
    mRecyclerView.setAdapter(mListingsAdapter);
  }

  private RecyclerView.OnScrollListener getOnScrollListener() {
    return new RecyclerView.OnScrollListener() {

      int mFirstVisibleItem;
      int mVisibleItemCount;
      int mTotalItemCount;

      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        LinearLayoutManager mgr = (LinearLayoutManager) recyclerView.getLayoutManager();
        mVisibleItemCount = mgr.getChildCount();
        mTotalItemCount = mgr.getItemCount();
        mFirstVisibleItem = mgr.findFirstVisibleItemPosition();
        if (mFirstVisibleItem == 0) {
          mCallbacks.onFirstItemShown();
        } else if ((mVisibleItemCount + mFirstVisibleItem) >= mTotalItemCount) {
          mCallbacks.onLastItemShown();
        }
      }
    };
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
  public void onDestroy() {
    super.onDestroy();
    mListingsPresenter.onViewDestroyed();
    mRecyclerView.setAdapter(null);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.listings, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_refresh:
        mListingsPresenter.refreshData();
        mAnalytics.logOptionRefresh();
        return true;
      case R.id.action_settings:
        mRedditNavigationView.showSettings();
        mAnalytics.logOptionSettings();
        return true;
    }
    return false;
  }

  public void showLinkContextMenu(ContextMenu menu, View v, Link link) {
    getActivity().getMenuInflater().inflate(R.menu.link_context, menu);

    // Build title for menu
    String score = link.getScore() == null ?
        v.getContext().getString(R.string.hidden_score_placeholder) : link.getScore().toString();
    String title = String.format(getString(R.string.menu_action_link), link.getTitle(), score);
    menu.setHeaderTitle(title);

    // Set state of hide/unhide
    menu.findItem(R.id.action_link_hide)
        .setVisible(!link.isHidden());
    menu.findItem(R.id.action_link_unhide)
        .setVisible(link.isHidden());

    // Set subreddit for link in the view subreddit menu item
    String subreddit = String.format(
        getString(R.string.action_view_subreddit), link.getSubreddit());
    menu.findItem(R.id.action_link_view_subreddit)
        .setTitle(subreddit);

    // Set username for link in the view user profile menu item
    String username = String.format(
        getString(R.string.action_view_user_profile), link.getAuthor());
    menu.findItem(R.id.action_link_view_user_profile)
        .setTitle(username);

    // Hide user profile for posts by deleted users
    if ("[deleted]".equalsIgnoreCase(link.getAuthor())) {
      menu.findItem(R.id.action_link_view_user_profile)
          .setVisible(false);
    }
  }

  public void showCommentContextMenu(ContextMenu menu, View v, Comment comment) {
    getActivity().getMenuInflater().inflate(R.menu.comment_context, menu);

    // Build title for menu
    String score = comment.getScore() == null ?
        v.getContext().getString(R.string.hidden_score_placeholder) : comment.getScore().toString();
    String title = String.format(getString(R.string.menu_action_comment),
        comment.getAuthor(), score);
    menu.setHeaderTitle(title);

    // Set username for listing in the user profile menu item
    String username = String.format(
        getString(R.string.action_view_user_profile), comment.getAuthor());
    menu.findItem(R.id.action_comment_view_user_profile).setTitle(username);

    // Hide user profile for posts by deleted users
    if ("[deleted]".equalsIgnoreCase(comment.getAuthor())) {
      menu.findItem(R.id.action_comment_view_user_profile).setVisible(false);
    }
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
      case R.id.action_link_view_subreddit:
        mLinkPresenter.openLinkSubreddit();
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
        Timber.w("No action registered to this context item");
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
    Uri uri = Uri.parse(LINK_BASE_URL + link.getPermalink());
    Intent i = new Intent(Intent.ACTION_VIEW, uri);
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(i);
  }

  public void openLinkInWebView(@NonNull Link link) {
    mAnalytics.logOpenLink(link);
    mRedditNavigationView.openURL(link.getUrl());
  }

  public void showCommentsForLink(
      @NonNull String subreddit, @NonNull String linkId, @Nullable String commentId) {
    mRedditNavigationView.showCommentsForLink(subreddit, linkId, commentId);
  }

  public void openReplyView(@NonNull Listing listing) {
    showToast(R.string.implementation_pending);
  }

  public void openShareView(@NonNull Comment comment) {
    Intent i = new Intent(Intent.ACTION_SEND);
    i.setType("text/plain");
    i.putExtra(Intent.EXTRA_TEXT, comment.getUrl());
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(i);
  }

  public void openSubredditView(String subreddit) {
    Intent intent = SubredditActivity.getIntent(getContext(), subreddit, null, null);
    startActivity(intent);
  }

  public void openUserProfileView(@NonNull Link link) {
    mRedditNavigationView.showUserProfile(link.getAuthor(), null, null);
  }

  public void openUserProfileView(@NonNull Comment comment) {
    mRedditNavigationView.showUserProfile(comment.getAuthor(), null, null);
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
  public void notifyItemRangeChanged(int position, int count) {
    mListingsAdapter.notifyItemRangeChanged(position, count);
  }

  @Override
  public void notifyItemRangeInserted(int position, int count) {
    mListingsAdapter.notifyItemRangeInserted(position, count);
  }

  @Override
  public void notifyItemRangeRemoved(int position, int count) {
    mListingsAdapter.notifyItemRangeRemoved(position, count);
  }

  @Override
  public void onRefresh() {
    mSwipeRefreshLayout.setRefreshing(false);
    mListingsPresenter.refreshData();
    mAnalytics.logOptionRefresh();
  }
}
