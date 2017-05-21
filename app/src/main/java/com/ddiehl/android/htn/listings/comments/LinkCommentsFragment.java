package com.ddiehl.android.htn.listings.comments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.listings.BaseListingsFragment;
import com.ddiehl.android.htn.listings.ListingsAdapter;
import com.ddiehl.android.htn.settings.SettingsManager;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import javax.inject.Inject;

import butterknife.BindView;
import rxreddit.model.Comment;
import rxreddit.model.Link;
import rxreddit.model.Listing;

import static android.app.Activity.RESULT_OK;
import static com.ddiehl.android.htn.utils.AndroidUtils.safeStartActivity;

@FragmentWithArgs
public class LinkCommentsFragment extends BaseListingsFragment
        implements LinkCommentsView, SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = LinkCommentsFragment.class.getSimpleName();

    private LinkCommentsPresenter mLinkCommentsPresenter;

    @Inject SettingsManager mSettingsManager;

    @Arg String mSubreddit;
    @Arg String mArticleId;
    @Arg String mCommentId;

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;

    private String mSort;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HoldTheNarwhal.getApplicationComponent().inject(this);
        FragmentArgs.inject(this);
        mLinkCommentsPresenter = new LinkCommentsPresenter(this, mRedditNavigationView, this);
        mListingsPresenter = mLinkCommentsPresenter;
        mSort = mSettingsManager.getCommentSort();
        mCallbacks = (Callbacks) mListingsPresenter;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(String.format(getString(R.string.link_subreddit), mSubreddit));
    }

    @Override
    public void onDestroy() {
        mLinkCommentsPresenter.clearData();
        notifyDataSetChanged();

        super.onDestroy();
    }

    @Override
    public String getCommentId() {
        return mCommentId;
    }

    @Override
    public String getArticleId() {
        return mArticleId;
    }

    @Override
    public String getSubreddit() {
        return mSubreddit;
    }

    @Override
    public String getSort() {
        return mSort;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.link_comments_fragment;
    }

    @Override
    protected ListingsAdapter getListingsAdapter() {
        return new LinkCommentsAdapter(this, mLinkCommentsPresenter);
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
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        safeStartActivity(getContext(), intent);
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
        mRedditNavigationView.showUserProfile(link.getAuthor(), null, null);
    }

    @Override
    public void openUserProfileView(@NonNull Comment comment) {
        mRedditNavigationView.showUserProfile(comment.getAuthor(), null, null);
    }

    @Override
    public void openLinkInBrowser(@NonNull Link link) {
        Uri uri = Uri.parse(link.getUrl());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        safeStartActivity(getContext(), intent);
    }

    @Override
    public void openCommentsInBrowser(@NonNull Link link) {
        Uri uri = Uri.parse("http://www.reddit.com" + link.getPermalink());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        safeStartActivity(getContext(), intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHOOSE_SORT:
                if (resultCode == RESULT_OK) {
                    String sort = data.getStringExtra(ChooseCommentSortDialog.EXTRA_SORT);
                    onSortSelected(sort);
                }
                break;
            case REQUEST_ADD_COMMENT:
                if (resultCode == RESULT_OK) {
                    String parentId = data.getStringExtra(AddCommentDialog.EXTRA_PARENT_ID);
                    String commentText = data.getStringExtra(AddCommentDialog.EXTRA_COMMENT_TEXT);
                    mLinkCommentsPresenter.onCommentSubmitted(commentText);
                }
                break;
        }
    }

    @Override
    public void refreshOptionsMenu() {
        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.comments, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        Link link = mLinkCommentsPresenter.getLinkContext();

        MenuItem viewSubredditItem = menu.findItem(R.id.action_link_view_subreddit);
        MenuItem viewUserItem = menu.findItem(R.id.action_link_view_user_profile);

        if (link == null) {
            viewSubredditItem.setVisible(false);
            viewUserItem.setVisible(false);
        } else {
            viewSubredditItem.setVisible(true);
            viewUserItem.setVisible(true);

            // Insert name of subreddit to its menu item
            String viewSubredditTitle = viewSubredditItem.getTitle().toString();
            viewSubredditItem.setTitle(
                    String.format(viewSubredditTitle, link.getSubreddit())
            );

            // Insert name of user in user profile menu item
            String viewUserTitle = viewUserItem.getTitle().toString();
            viewUserItem.setTitle(
                    String.format(viewUserTitle, link.getAuthor())
            );
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Link link = mLinkCommentsPresenter.getLinkContext();

        switch (item.getItemId()) {
            case R.id.action_share:
                mListingsPresenter.shareLink(link);
                return true;
            case R.id.action_change_sort:
                showSortOptionsMenu();
                return true;
            case R.id.action_link_view_subreddit:
                mListingsPresenter.openLinkSubreddit(link);
                return true;
            case R.id.action_link_view_user_profile:
                mListingsPresenter.openLinkUserProfile(link);
                return true;
            case R.id.action_link_open_in_browser:
                mListingsPresenter.openLinkInBrowser(link);
                return true;
            case R.id.action_link_open_comments_in_browser:
                mListingsPresenter.openCommentsInBrowser(link);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSortOptionsMenu() {
        ChooseCommentSortDialog chooseLinkSortDialog =
                ChooseCommentSortDialog.newInstance(mSort);
        chooseLinkSortDialog.setTargetFragment(this, REQUEST_CHOOSE_SORT);
        chooseLinkSortDialog.show(getFragmentManager(), ChooseCommentSortDialog.TAG);
    }

    private void onSortSelected(@NonNull String sort) {
        if (sort.equals(mSort)) return;

        mSort = sort;
        mListingsPresenter.onSortChanged();
    }

    @Override
    public void openLinkInWebView(@NonNull Link link) {
        mRedditNavigationView.openURL(link.getUrl());
    }

    @Override
    public void showCommentsForLink(
            @NonNull String subreddit, @NonNull String linkId, @Nullable String commentId) {
        mRedditNavigationView.showCommentsForLink(subreddit, linkId, commentId);
    }

    @Override
    public void openReplyView(@NonNull Listing listing) {
        String id = listing.getKind() + "_" + listing.getId();
        AddCommentDialog dialog = AddCommentDialog.newInstance(id);
        dialog.setTargetFragment(this, REQUEST_ADD_COMMENT);
        dialog.show(getFragmentManager(), AddCommentDialog.TAG);
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(false);
        mLinkCommentsPresenter.refreshData();
    }

    @Override
    protected View getChromeView() {
        return mCoordinatorLayout;
    }

    @Override
    public void showLinkContextMenu(ContextMenu menu, View view, Link link) {
        super.showLinkContextMenu(menu, view, link);

        // Show the reply action when viewing link comments
        menu.findItem(R.id.action_link_reply).setVisible(true);
    }

    /**
     * Overriding the below methods to account for the presence
     * of a link at the top of the adapter
     */

    @Override
    public void notifyItemChanged(int position) {
        super.notifyItemChanged(position + 1);
    }

    @Override
    public void notifyItemInserted(int position) {
        super.notifyItemInserted(position + 1);
    }

    @Override
    public void notifyItemRemoved(int position) {
        super.notifyItemRemoved(position + 1);
    }

    @Override
    public void notifyItemRangeChanged(int position, int count) {
        super.notifyItemRangeChanged(position + 1, count);
    }

    @Override
    public void notifyItemRangeInserted(int position, int count) {
        super.notifyItemRangeInserted(position + 1, count);
    }

    @Override
    public void notifyItemRangeRemoved(int position, int count) {
        super.notifyItemRangeRemoved(position + 1, count);
    }
}
