package com.ddiehl.android.htn.view.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IntDef;
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
import com.ddiehl.android.htn.presenter.LinkCommentsPresenter;
import com.ddiehl.android.htn.presenter.LinkCommentsPresenterImpl;
import com.ddiehl.android.htn.view.LinkCommentsView;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.adapters.LinkCommentsAdapter;
import com.ddiehl.android.htn.view.dialogs.AddCommentDialog;
import com.ddiehl.android.htn.view.dialogs.ChooseCommentSortDialog;
import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.Link;
import com.ddiehl.reddit.listings.Listing;

import butterknife.ButterKnife;

public class LinkCommentsFragment extends Fragment
    implements LinkCommentsView, SwipeRefreshLayout.OnRefreshListener {
  private static final String ARG_SUBREDDIT = "subreddit";
  private static final String ARG_ARTICLE = "article";
  private static final String ARG_COMMENT_ID = "comment_id";

  @IntDef({REQUEST_CHOOSE_SORT, REQUEST_ADD_COMMENT})
  public @interface RequestCode {
  }
  private static final int REQUEST_CHOOSE_SORT = 0;
  private static final int REQUEST_ADD_COMMENT = 1;
  private static final String DIALOG_CHOOSE_SORT = "dialog_choose_sort";
  private static final String DIALOG_ADD_COMMENT = "add_comment_dialog";

  private Analytics mAnalytics = HoldTheNarwhal.getAnalytics();
  private MainView mMainView;
  private LinkCommentsPresenter mLinkCommentsPresenter;

  private LinkCommentsAdapter mLinkCommentsAdapter;
  private SwipeRefreshLayout mSwipeRefreshLayout;

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
    mMainView = (MainView) getActivity();
    mLinkCommentsPresenter = new LinkCommentsPresenterImpl(
        mMainView, this, subreddit, articleId, commentId);
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
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mSwipeRefreshLayout = ButterKnife.findById(view, R.id.swipe_refresh_layout);
    mSwipeRefreshLayout.setOnRefreshListener(this);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    mLinkCommentsPresenter.onViewDestroyed();
  }

  private void updateTitle() {
    Link link = mLinkCommentsPresenter.getLinkContext();
    if (link != null) {
      mMainView.setTitle(link.getTitle());
    } else {
      mMainView.setTitle(getString(R.string.comments_fragment_title));
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    mLinkCommentsPresenter.onResume();
  }

  @Override
  public void onPause() {
    mLinkCommentsPresenter.onPause();
    super.onPause();
  }

  @Override
  public void showLinkContextMenu(ContextMenu menu, View v, Link link) {
    getActivity().getMenuInflater().inflate(R.menu.link_context_menu, menu);
    String title = String.format(v.getContext().getString(R.string.menu_action_link),
        link.getTitle(), link.getScore());
    menu.setHeaderTitle(title);
    menu.findItem(R.id.action_link_reply).setVisible(true);
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
  public void commentAddedAt(int position) {
    mLinkCommentsAdapter.notifyItemInserted(position + 1);
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
  public void showCommentThread(
      @Nullable String subreddit, @Nullable String linkId, @NonNull String commentId) {
    mLinkCommentsPresenter.showCommentThread(subreddit, linkId, commentId);
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
  public void openCommentInBrowser(@NonNull Comment comment) {
    Uri uri = Uri.parse(comment.getUrl());
    Intent i = new Intent(Intent.ACTION_VIEW, uri);
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(i);
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
  public void openUserProfileView(@NonNull Link link) {
    ((MainView) getActivity()).showUserProfile(link.getAuthor(), "summary");
  }

  @Override
  public void openUserProfileView(@NonNull Comment comment) {
    ((MainView) getActivity()).showUserProfile(comment.getAuthor(), "summary");
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
  public boolean onContextItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_link_reply:
        mLinkCommentsPresenter.replyToLink();
        return true;
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
        mLinkCommentsPresenter.replyToComment();
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
  public void onActivityResult(@RequestCode int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUEST_CHOOSE_SORT:
        if (resultCode == Activity.RESULT_OK) {
          String sort = data.getStringExtra(ChooseCommentSortDialog.EXTRA_SORT);
          mAnalytics.logOptionChangeSort(sort);
          mLinkCommentsPresenter.updateSort(sort);
        }
        getActivity().invalidateOptionsMenu();
        break;
      case REQUEST_ADD_COMMENT:
        if (resultCode == Activity.RESULT_OK) {
          String parentId = data.getStringExtra(AddCommentDialog.EXTRA_PARENT_ID);
          String commentText = data.getStringExtra(AddCommentDialog.EXTRA_COMMENT_TEXT);
          mLinkCommentsPresenter.onCommentSubmitted(commentText);
        }
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
        mAnalytics.logOptionChangeSort();
        return true;
      case R.id.action_refresh:
        mLinkCommentsPresenter.requestData();
        mAnalytics.logOptionRefresh();
        return true;
      case R.id.action_settings:
        mMainView.showSettings();
        mAnalytics.logOptionSettings();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void showChooseCommentSortDialog() {
    FragmentManager fm = getActivity().getFragmentManager();
    String currentSort = HoldTheNarwhal.getSettingsManager().getCommentSort();
    ChooseCommentSortDialog chooseCommentSortDialog = ChooseCommentSortDialog.newInstance(currentSort);
    chooseCommentSortDialog.setTargetFragment(this, REQUEST_CHOOSE_SORT);
    chooseCommentSortDialog.show(fm, DIALOG_CHOOSE_SORT);
  }

  @Override
  public void openLinkInWebView(@NonNull Link link) {
    ((MainView) getActivity()).openURL(link.getUrl());
  }

  @Override
  public void showCommentsForLink(
      @Nullable String subreddit, @Nullable String linkId, @Nullable String commentId) {
    mMainView.showCommentsForLink(subreddit, linkId, commentId);
  }

  @Override
  public void openReplyView(@NonNull Listing listing) {
    String id = listing.getKind() + "_" + listing.getId();
    AddCommentDialog dialog = AddCommentDialog.newInstance(id);
    dialog.setTargetFragment(this, REQUEST_ADD_COMMENT);
    dialog.show(getFragmentManager(), DIALOG_ADD_COMMENT);
  }

  @Override
  public void onRefresh() {
    mSwipeRefreshLayout.setRefreshing(false);
    mLinkCommentsPresenter.requestData();
    mAnalytics.logOptionRefresh();
  }
}
