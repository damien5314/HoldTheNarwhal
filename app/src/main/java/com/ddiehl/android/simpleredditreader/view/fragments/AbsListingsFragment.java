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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.ddiehl.android.simpleredditreader.BusProvider;
import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.presenter.ListingsPresenter;
import com.ddiehl.android.simpleredditreader.view.ListingsView;
import com.ddiehl.android.simpleredditreader.view.MainView;
import com.ddiehl.android.simpleredditreader.view.activities.MainActivity;
import com.ddiehl.android.simpleredditreader.view.adapters.ListingsAdapter;
import com.ddiehl.android.simpleredditreader.view.dialogs.ChooseLinkSortDialog;
import com.ddiehl.android.simpleredditreader.view.dialogs.ChooseTimespanDialog;
import com.ddiehl.reddit.listings.RedditComment;
import com.ddiehl.reddit.listings.RedditLink;
import com.mopub.nativeads.MoPubAdRecycleAdapter;
import com.mopub.nativeads.MoPubNativeAdRenderer;
import com.mopub.nativeads.RequestParameters;
import com.mopub.nativeads.ViewBinder;
import com.squareup.otto.Bus;

import java.util.EnumSet;

import butterknife.ButterKnife;

public abstract class AbsListingsFragment extends AbsRedditFragment implements ListingsView {
    private static final String TAG = AbsListingsFragment.class.getSimpleName();

    private static final int REQUEST_CHOOSE_SORT = 0;
    private static final int REQUEST_CHOOSE_TIMESPAN = 1;
    private static final String DIALOG_CHOOSE_SORT = "dialog_choose_sort";
    private static final String DIALOG_CHOOSE_TIMESPAN = "dialog_choose_timespan";

    protected Bus mBus = BusProvider.getInstance();

    protected ListingsPresenter mListingsPresenter;
    protected ListingsAdapter mListingsAdapter;

    private int mFirstVisibleItem, mVisibleItemCount, mTotalItemCount;
    protected String mSelectedSort, mSelectedTimespan;

    protected MoPubAdRecycleAdapter mAdAdapter;
    protected RequestParameters mAdRequestParameters;

    abstract void updateTitle();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        mBus = BusProvider.getInstance();
    }

    protected void instantiateListView(View v) {
        RecyclerView rv = ButterKnife.findById(v, R.id.recycler_view);
        final LinearLayoutManager mgr = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(mgr);
        rv.clearOnScrollListeners();
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        // Set up ViewBinder and MoPubAdRecycleAdapter for MoPub ads
        ViewBinder vb = new ViewBinder.Builder(R.layout.listings_banner_ad)
                .iconImageId(R.id.ad_icon)
                .titleId(R.id.ad_title)
                .textId(R.id.ad_text)
                .callToActionId(R.id.ad_cta)
                .build();
        MoPubNativeAdRenderer renderer = new MoPubNativeAdRenderer(vb);
        mAdAdapter = new MoPubAdRecycleAdapter(getActivity(), mListingsAdapter);
        mAdAdapter.registerAdRenderer(renderer);
        rv.setAdapter(mAdAdapter);

        // Set configuration for MoPub ads
        final EnumSet<RequestParameters.NativeAdAsset> desiredAssets = EnumSet.of(
                RequestParameters.NativeAdAsset.ICON_IMAGE,
                RequestParameters.NativeAdAsset.TITLE,
                RequestParameters.NativeAdAsset.TEXT,
                RequestParameters.NativeAdAsset.CALL_TO_ACTION_TEXT
        );

        mAdRequestParameters = new RequestParameters.Builder()
                .desiredAssets(desiredAssets)
                .location(null)
                .build();
    }

    @Override
    public void onResume() {
        super.onResume();
        mBus.register(mListingsPresenter);

        if (mListingsAdapter.getItemCount() == 0) {
            mListingsPresenter.refreshData();
        }

        mAdAdapter.loadAds(getString(R.string.ads_listings_banner_id_mopub), mAdRequestParameters);
    }

    @Override
    public void onPause() {
        super.onPause();
        mBus.unregister(mListingsPresenter);
    }

    @Override
    public void onDestroy() {
        mAdAdapter.destroy();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHOOSE_SORT:
                if (resultCode == Activity.RESULT_OK) {
                    mSelectedSort = data.getStringExtra(ChooseLinkSortDialog.EXTRA_SORT);
                    if (mSelectedSort.equals("top") || mSelectedSort.equals("controversial")) {
                        showTimespanOptionsMenu();
                    } else {
                        mListingsPresenter.updateSort(mSelectedSort, mSelectedTimespan);
                        getActivity().supportInvalidateOptionsMenu();
                    }
                }
                break;
            case REQUEST_CHOOSE_TIMESPAN:
                if (resultCode == Activity.RESULT_OK) {
                    mSelectedTimespan = data.getStringExtra(ChooseTimespanDialog.EXTRA_TIMESPAN);
                    mListingsPresenter.updateSort(mSelectedSort, mSelectedTimespan);
                    getActivity().supportInvalidateOptionsMenu();
                }
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.links_menu, menu);

        // Disable timespan option if current sort does not support it
        String sort = mListingsPresenter.getSort();
        if (sort.equals("hot") || sort.equals("new") || sort.equals("rising")) {
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
                return true;
            case R.id.action_change_timespan:
                showTimespanOptionsMenu();
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

    protected void showSortOptionsMenu() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        ChooseLinkSortDialog chooseLinkSortDialog = ChooseLinkSortDialog.newInstance(mListingsPresenter.getSort());
        chooseLinkSortDialog.setTargetFragment(this, REQUEST_CHOOSE_SORT);
        chooseLinkSortDialog.show(fm, DIALOG_CHOOSE_SORT);
    }

    protected void showTimespanOptionsMenu() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        ChooseTimespanDialog chooseTimespanDialog = ChooseTimespanDialog.newInstance(mListingsPresenter.getTimespan());
        chooseTimespanDialog.setTargetFragment(this, REQUEST_CHOOSE_TIMESPAN);
        chooseTimespanDialog.show(fm, DIALOG_CHOOSE_TIMESPAN);
    }

    @Override
    public void showLinkContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, RedditLink link) {
        getActivity().getMenuInflater().inflate(R.menu.link_context_menu, menu);
        String title = String.format(v.getContext().getString(R.string.menu_action_link),
                link.getTitle(), link.getScore());
        menu.setHeaderTitle(title);
        menu.findItem(R.id.action_link_hide).setVisible(!link.isHidden());
        menu.findItem(R.id.action_link_unhide).setVisible(link.isHidden());
        // Set username for listing in the user profile menu item
        String username = String.format(getString(R.string.action_view_user_profile), link.getAuthor());
        menu.findItem(R.id.action_link_view_user_profile).setTitle(username);
    }

    @Override
    public void showCommentContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, RedditComment comment) {
        getActivity().getMenuInflater().inflate(R.menu.comment_context_menu, menu);
        String title = String.format(getString(R.string.menu_action_comment),
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
    public void openWebViewForLink(RedditLink link) {
        ((MainActivity) getActivity()).showWebViewForURL(link.getUrl());
    }

    @Override
    public void showCommentsForLink(String subreddit, String id) {
        Fragment fragment = LinkCommentsFragment.newInstance(subreddit, id, null);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void navigateToCommentThread(String commentId) {
        mListingsPresenter.navigateToCommentThread(commentId);
    }

    @Override
    public void openReplyView(RedditComment comment) {
        showToast(R.string.implementation_pending);
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
    public void openUserProfileView(RedditLink link) {
        ((MainView) getActivity()).showUserProfile("overview", link.getAuthor());
    }

    @Override
    public void openUserProfileView(RedditComment comment) {
        ((MainView) getActivity()).showUserProfile("overview", comment.getAuthor());
    }

    @Override
    public void openCommentInBrowser(RedditComment comment) {
        Uri uri = Uri.parse(comment.getUrl());
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public void listingsUpdated() {
        mListingsAdapter.notifyDataSetChanged();
        updateTitle();
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
