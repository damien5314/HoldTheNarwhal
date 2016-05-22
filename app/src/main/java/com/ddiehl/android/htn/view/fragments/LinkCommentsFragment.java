package com.ddiehl.android.htn.view.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.analytics.Analytics;
import com.ddiehl.android.htn.presenter.LinkCommentsPresenter;
import com.ddiehl.android.htn.presenter.LinkCommentsPresenterImpl;
import com.ddiehl.android.htn.view.LinkCommentsView;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.adapters.LinkCommentsAdapter;
import com.ddiehl.android.htn.view.adapters.ListingsAdapter;
import com.ddiehl.android.htn.view.dialogs.AddCommentDialog;

import javax.inject.Inject;

import rxreddit.model.Comment;
import rxreddit.model.Link;
import rxreddit.model.Listing;

public class LinkCommentsFragment extends BaseListingsFragment
    implements LinkCommentsView, SwipeRefreshLayout.OnRefreshListener {
  private static final String ARG_SUBREDDIT = "arg_subreddit";
  private static final String ARG_ARTICLE = "arg_article";
  private static final String ARG_COMMENT_ID = "arg_comment_id";

  private static final int REQUEST_ADD_COMMENT = 0x00000001;
  private static final String DIALOG_ADD_COMMENT = "add_comment_dialog";

  @Inject protected Analytics mAnalytics;
  private LinkCommentsPresenter mLinkCommentsPresenter;

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
    mListingsPresenter = mLinkCommentsPresenter;
    mCallbacks = (Callbacks) mListingsPresenter;
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.link_comments_fragment;
  }

  @Override
  protected ListingsAdapter getListingsAdapter() {
    return new LinkCommentsAdapter(mLinkCommentsPresenter);
  }

  @Override
  public void showCommentContextMenu(ContextMenu menu, View v, Comment comment) {
    getActivity().getMenuInflater().inflate(R.menu.comment_context, menu);
    String score = comment.getScore() == null ?
        v.getContext().getString(R.string.hidden_score_placeholder) : comment.getScore().toString();
    String title = String.format(getString(R.string.menu_action_comment),
        comment.getAuthor(), score);
    menu.setHeaderTitle(title);
    if (comment.isArchived()) {
      menu.findItem(R.id.action_comment_report).setVisible(false);
    }
    // Set username for listing in the user profile menu item
    String username = String.format(getString(R.string.action_view_user_profile), comment.getAuthor());
    menu.findItem(R.id.action_comment_view_user_profile).setTitle(username);
    if ("[deleted]".equalsIgnoreCase(comment.getAuthor())) {
      menu.findItem(R.id.action_comment_view_user_profile).setVisible(false);
    }
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
    ((MainView) getActivity()).showUserProfile(link.getAuthor(), null, null);
  }

  @Override
  public void openUserProfileView(@NonNull Comment comment) {
    ((MainView) getActivity()).showUserProfile(comment.getAuthor(), null, null);
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
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
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
    inflater.inflate(R.menu.comments, menu);
  }

  @Override
  public void openLinkInWebView(@NonNull Link link) {
    ((MainView) getActivity()).openURL(link.getUrl());
  }

  @Override
  public void showCommentsForLink(
      @NonNull String subreddit, @NonNull String linkId, @Nullable String commentId) {
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
    mLinkCommentsPresenter.refreshData();
    mAnalytics.logOptionRefresh();
  }

  @Override
  public void linkUpdated() {
    mListingsAdapter.notifyItemChanged(0);
  }

  /**
   * Overriding the below methods to account for the presence
   * of a link at the top of the adapter
   */

  @Override
  public void notifyItemChanged(int position) {
    super.notifyItemChanged(position+1);
  }

  @Override
  public void notifyItemInserted(int position) {
    super.notifyItemInserted(position+1);
  }

  @Override
  public void notifyItemRemoved(int position) {
    super.notifyItemRemoved(position+1);
  }

  @Override
  public void notifyItemRangeChanged(int position, int count) {
    super.notifyItemRangeChanged(position+1, count);
  }

  @Override
  public void notifyItemRangeInserted(int position, int count) {
    super.notifyItemRangeInserted(position+1, count);
  }

  @Override
  public void notifyItemRangeRemoved(int position, int count) {
    super.notifyItemRangeRemoved(position+1, count);
  }
}
