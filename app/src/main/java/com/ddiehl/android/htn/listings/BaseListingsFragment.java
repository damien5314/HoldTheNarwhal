package com.ddiehl.android.htn.listings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import com.ddiehl.android.htn.listings.report.ReportActivity;
import com.ddiehl.android.htn.listings.subreddit.SubredditActivity;
import com.ddiehl.android.htn.view.BaseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import rxreddit.model.Comment;
import rxreddit.model.Link;
import rxreddit.model.Listing;
import rxreddit.model.PrivateMessage;
import timber.log.Timber;

import static com.ddiehl.android.htn.listings.report.ReportActivity.RESULT_REPORT_ERROR;
import static com.ddiehl.android.htn.listings.report.ReportActivity.RESULT_REPORT_SUCCESS;
import static com.ddiehl.android.htn.utils.AndroidUtils.safeStartActivity;

public abstract class BaseListingsFragment extends BaseFragment
        implements ListingsView, SwipeRefreshLayout.OnRefreshListener {

    private static final int REQUEST_REPORT_LISTING = 1000;

    private static final String LINK_BASE_URL = "http://www.reddit.com";

    @BindView(R.id.recycler_view) protected RecyclerView recyclerView;

    protected BaseListingsPresenter listingsPresenter;
    protected ListingsAdapter listingsAdapter;
    protected ListingsView.Callbacks callbacks;

    protected SwipeRefreshLayout swipeRefreshLayout;
    protected Listing listingSelected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HoldTheNarwhal.getApplicationComponent().inject(this);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @NonNull @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        View view = super.onCreateView(inflater, container, state);

        // Initialize list view
        listingsAdapter = getListingsAdapter();
        instantiateListView();

        return view;
    }

    protected abstract ListingsAdapter getListingsAdapter();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout = ButterKnife.findById(view, R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    private void instantiateListView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.clearOnScrollListeners();
        recyclerView.addOnScrollListener(getOnScrollListener());
        recyclerView.setAdapter(listingsAdapter);
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
                    callbacks.onFirstItemShown();
                } else if ((mVisibleItemCount + mFirstVisibleItem) >= mTotalItemCount) {
                    callbacks.onLastItemShown();
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();

        // FIXME Do we need to check mNextRequested here?
        if (!listingsPresenter.hasData()) {
            listingsPresenter.refreshData();
        }
    }

    @Override
    public void onDestroy() {
        recyclerView.setAdapter(null);

        // To disable the memory dereferencing functionality just comment these lines
        listingsPresenter.clearData();

        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.listings, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                listingsPresenter.refreshData();
                return true;
            case R.id.action_settings:
                redditNavigationView.showSettings();
                return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_REPORT_LISTING:
                switch (resultCode) {
                    case RESULT_REPORT_SUCCESS:
                        showReportSuccessToast(listingSelected);
                        break;
                    case RESULT_REPORT_ERROR:
                        showReportErrorToast(listingSelected);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    void showReportSuccessToast(@NonNull Listing listing) {
        Snackbar.make(getChromeView(), R.string.report_successful, Snackbar.LENGTH_LONG)
                .show();
    }

    void showReportErrorToast(@NonNull Listing listing) {
        Snackbar.make(getChromeView(), R.string.report_error, Snackbar.LENGTH_LONG)
                .show();
    }

    public void showLinkContextMenu(ContextMenu menu, View view, Link link) {
        listingSelected = link;
        getActivity().getMenuInflater().inflate(R.menu.link_context, menu);

        // Build title for menu
        String score = link.getScore() == null ?
                view.getContext().getString(R.string.hidden_score_placeholder) : link.getScore().toString();
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

        menu.findItem(R.id.action_link_reply).setVisible(false);
        menu.findItem(R.id.action_link_save).setVisible(!link.isSaved());
        menu.findItem(R.id.action_link_unsave).setVisible(link.isSaved());
    }

    public void showCommentContextMenu(ContextMenu menu, View view, Comment comment) {
        listingSelected = comment;
        getActivity().getMenuInflater().inflate(R.menu.comment_context, menu);

        // Build title for menu
        String score = comment.getScore() == null ?
                view.getContext().getString(R.string.hidden_score_placeholder) : comment.getScore().toString();
        String title = String.format(getString(R.string.menu_action_comment),
                comment.getAuthor(), score);
        menu.setHeaderTitle(title);

        // Set username for listing in the user profile menu item
        String username = String.format(
                getString(R.string.action_view_user_profile), comment.getAuthor());
        menu.findItem(R.id.action_comment_view_user_profile).setTitle(username);

        // Hide save/unsave option
        menu.findItem(R.id.action_comment_save).setVisible(!comment.isSaved());
        menu.findItem(R.id.action_comment_unsave).setVisible(comment.isSaved());

        // Hide user profile for posts by deleted users
        if ("[deleted]".equalsIgnoreCase(comment.getAuthor())) {
            menu.findItem(R.id.action_comment_view_user_profile).setVisible(false);
        }

        // Don't show parent menu option if there is no parent
        if (comment.getLinkId().equals(comment.getParentId())) {
            menu.findItem(R.id.action_comment_parent).setVisible(false);
        }
    }

    public void showMessageContextMenu(ContextMenu menu, View view, PrivateMessage message) {
        listingSelected = message;
        getActivity().getMenuInflater().inflate(R.menu.message_context, menu);

        // Hide ride/unread option based on state
        menu.findItem(R.id.action_message_mark_read)
                .setVisible(message.isUnread());
        menu.findItem(R.id.action_message_mark_unread)
                .setVisible(!message.isUnread());
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_link_reply:
                listingsPresenter.replyToLink((Link) listingSelected);
                return true;
            case R.id.action_link_upvote:
                listingsPresenter.upvoteLink((Link) listingSelected);
                return true;
            case R.id.action_link_downvote:
                listingsPresenter.downvoteLink((Link) listingSelected);
                return true;
            case R.id.action_link_show_comments:
                listingsPresenter.showCommentsForLink((Link) listingSelected);
                return true;
            case R.id.action_link_save:
                listingsPresenter.saveLink((Link) listingSelected);
                return true;
            case R.id.action_link_unsave:
                listingsPresenter.unsaveLink((Link) listingSelected);
                return true;
            case R.id.action_link_share:
                listingsPresenter.shareLink((Link) listingSelected);
                return true;
            case R.id.action_link_view_subreddit:
                listingsPresenter.openLinkSubreddit((Link) listingSelected);
                return true;
            case R.id.action_link_view_user_profile:
                listingsPresenter.openLinkUserProfile((Link) listingSelected);
                return true;
            case R.id.action_link_open_in_browser:
                listingsPresenter.openLinkInBrowser((Link) listingSelected);
                return true;
            case R.id.action_link_open_comments_in_browser:
                listingsPresenter.openCommentsInBrowser((Link) listingSelected);
                return true;
            case R.id.action_link_hide:
                listingsPresenter.hideLink((Link) listingSelected);
                return true;
            case R.id.action_link_unhide:
                listingsPresenter.unhideLink((Link) listingSelected);
                return true;
            case R.id.action_link_report:
                listingsPresenter.reportLink((Link) listingSelected);
                return true;
            case R.id.action_comment_permalink:
                listingsPresenter.openCommentPermalink((Comment) listingSelected);
                return true;
            case R.id.action_comment_parent:
                listingsPresenter.openCommentParent((Comment) listingSelected);
                return true;
            case R.id.action_comment_reply:
                listingsPresenter.replyToComment((Comment) listingSelected);
                return true;
            case R.id.action_comment_upvote:
                listingsPresenter.upvoteComment((Comment) listingSelected);
                return true;
            case R.id.action_comment_downvote:
                listingsPresenter.downvoteComment((Comment) listingSelected);
                return true;
            case R.id.action_comment_save:
                listingsPresenter.saveComment((Comment) listingSelected);
                return true;
            case R.id.action_comment_unsave:
                listingsPresenter.unsaveComment((Comment) listingSelected);
                return true;
            case R.id.action_comment_share:
                listingsPresenter.shareComment((Comment) listingSelected);
                return true;
            case R.id.action_comment_view_user_profile:
                listingsPresenter.openCommentUserProfile((Comment) listingSelected);
                return true;
            case R.id.action_comment_open_in_browser:
                listingsPresenter.openCommentInBrowser((Comment) listingSelected);
                return true;
            case R.id.action_comment_report:
                listingsPresenter.reportComment((Comment) listingSelected);
                return true;
            case R.id.action_message_show_permalink:
                listingsPresenter.showMessagePermalink((PrivateMessage) listingSelected);
                return true;
            case R.id.action_message_report:
                listingsPresenter.reportMessage((PrivateMessage) listingSelected);
                return true;
            case R.id.action_message_block_user:
                listingsPresenter.blockUser((PrivateMessage) listingSelected);
                return true;
            case R.id.action_message_mark_read:
                listingsPresenter.markMessageRead((PrivateMessage) listingSelected);
                return true;
            case R.id.action_message_mark_unread:
                listingsPresenter.markMessageUnread((PrivateMessage) listingSelected);
                return true;
            case R.id.action_message_reply:
                listingsPresenter.replyToMessage((PrivateMessage) listingSelected);
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
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        safeStartActivity(getContext(), intent);
    }

    public void openCommentsInBrowser(@NonNull Link link) {
        Uri uri = Uri.parse(LINK_BASE_URL + link.getPermalink());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        safeStartActivity(getContext(), intent);
    }

    public void openUrlInWebView(@NonNull String url) {
        redditNavigationView.openURL(url);
    }

    public void showCommentsForLink(
            @NonNull String subreddit, @NonNull String linkId, @Nullable String commentId) {
        redditNavigationView.showCommentsForLink(subreddit, linkId, commentId);
    }

    public void openReplyView(@NonNull Listing listing) {
        showToast(getString(R.string.implementation_pending));
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
        redditNavigationView.showUserProfile(link.getAuthor(), null, null);
    }

    public void openUserProfileView(@NonNull Comment comment) {
        redditNavigationView.showUserProfile(comment.getAuthor(), null, null);
    }

    public void openCommentInBrowser(@NonNull Comment comment) {
        Uri uri = Uri.parse(comment.getUrl());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        safeStartActivity(getContext(), intent);
    }

    public void openReportView(@NonNull Link link) {
        Intent intent = ReportActivity.getIntent(
                getContext(), link.getFullName(), link.getSubreddit()
        );
        startActivityForResult(intent, REQUEST_REPORT_LISTING);
    }

    public void openReportView(@NonNull Comment comment) {
        Intent intent = ReportActivity.getIntent(
                getContext(), comment.getFullName(), comment.getSubreddit()
        );
        startActivityForResult(intent, REQUEST_REPORT_LISTING);
    }

    public void openReportView(@NonNull PrivateMessage message) {
        Intent intent = ReportActivity.getIntent(
                getContext(), message.getFullname(), null
        );
        startActivityForResult(intent, REQUEST_REPORT_LISTING);
    }

    @Override
    public void notifyDataSetChanged() {
        listingsAdapter.notifyDataSetChanged();
    }

    @Override
    public void notifyItemChanged(int position) {
        listingsAdapter.notifyItemChanged(position);
    }

    @Override
    public void notifyItemInserted(int position) {
        listingsAdapter.notifyItemInserted(position);
    }

    @Override
    public void notifyItemRemoved(int position) {
        listingsAdapter.notifyItemRemoved(position);
    }

    @Override
    public void notifyItemRangeChanged(int position, int count) {
        listingsAdapter.notifyItemRangeChanged(position, count);
    }

    @Override
    public void notifyItemRangeInserted(int position, int count) {
        listingsAdapter.notifyItemRangeInserted(position, count);
    }

    @Override
    public void notifyItemRangeRemoved(int position, int count) {
        listingsAdapter.notifyItemRangeRemoved(position, count);
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(false);
        listingsPresenter.refreshData();
    }
}
