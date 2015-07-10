/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.ddiehl.android.htn.BuildConfig;
import com.ddiehl.android.htn.BusProvider;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.SettingsManager;
import com.ddiehl.android.htn.presenter.ListingsPresenter;
import com.ddiehl.android.htn.utils.NUtils;
import com.ddiehl.android.htn.view.ListingsView;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.activities.MainActivity;
import com.ddiehl.android.htn.view.adapters.ListingsAdapter;
import com.ddiehl.android.htn.view.dialogs.ChooseLinkSortDialog;
import com.ddiehl.android.htn.view.dialogs.ChooseTimespanDialog;
import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.Link;
import com.flurry.android.FlurryAgent;
import com.mopub.nativeads.MoPubAdRecycleAdapter;
import com.mopub.nativeads.MoPubNativeAdRenderer;
import com.mopub.nativeads.RequestParameters;
import com.mopub.nativeads.ViewBinder;
import com.squareup.otto.Bus;

import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;

public abstract class AbsListingsFragment extends AbsRedditFragment
        implements ListingsView {
    private static final String TAG = AbsListingsFragment.class.getSimpleName();

    private static final int REQUEST_CHOOSE_SORT = 0;
    private static final int REQUEST_CHOOSE_TIMESPAN = 1;
    private static final String DIALOG_CHOOSE_SORT = "dialog_choose_sort";
    private static final String DIALOG_CHOOSE_TIMESPAN = "dialog_choose_timespan";

    Bus mBus = BusProvider.getInstance();

    ListingsPresenter mListingsPresenter;
    ListingsAdapter mListingsAdapter;

    private int mFirstVisibleItem, mVisibleItemCount, mTotalItemCount;
    String mSelectedSort, mSelectedTimespan;

    MoPubAdRecycleAdapter mAdAdapter;
    RequestParameters mAdRequestParameters;

    abstract void updateTitle();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        mBus = BusProvider.getInstance();
    }

    void instantiateListView(View v) {
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
                .build();
        MoPubNativeAdRenderer renderer = new MoPubNativeAdRenderer(vb);
        mAdAdapter = new MoPubAdRecycleAdapter(getActivity(), mListingsAdapter);
        mAdAdapter.registerAdRenderer(renderer);
        rv.setAdapter(mAdAdapter);

        // Set configuration for MoPub ads
        mAdRequestParameters = new RequestParameters.Builder()
                .desiredAssets(EnumSet.of(
                        RequestParameters.NativeAdAsset.ICON_IMAGE,
                        RequestParameters.NativeAdAsset.TITLE,
                        RequestParameters.NativeAdAsset.TEXT
                ))
                .build();
    }

    @Override
    public void onResume() {
        super.onResume();
        mBus.register(mListingsPresenter);

        if (mListingsAdapter.getItemCount() == 0) {
            mListingsPresenter.refreshData();
        }

//        loadAdsIfEnabled();
    }

    private void loadAdsIfEnabled() {
        boolean adsEnabled = SettingsManager.getInstance(getActivity()).getAdsEnabled();
        if (adsEnabled) {
            String key = NUtils.getMoPubApiKey(BuildConfig.DEBUG);
            mAdAdapter.loadAds(key, mAdRequestParameters);
        } else {
            mAdAdapter.clearAds();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mBus.unregister(mListingsPresenter);
    }

    @Override
    public void onDestroy() {
        if (mAdAdapter != null) {
            mAdAdapter.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHOOSE_SORT:
                if (resultCode == Activity.RESULT_OK) {
                    mSelectedSort = data.getStringExtra(ChooseLinkSortDialog.EXTRA_SORT);
                    FlurryAgent.logEvent("option - change sort - " + mSelectedSort);
                    if (mSelectedSort.equals("top") || mSelectedSort.equals("controversial")) {
                        showTimespanOptionsMenu();
                    } else {
                        mListingsPresenter.updateSort(mSelectedSort, mSelectedTimespan);
                        getActivity().invalidateOptionsMenu();
                    }
                }
                break;
            case REQUEST_CHOOSE_TIMESPAN:
                if (resultCode == Activity.RESULT_OK) {
                    mSelectedTimespan = data.getStringExtra(ChooseTimespanDialog.EXTRA_TIMESPAN);
                    FlurryAgent.logEvent("option - change timespan - " + mSelectedTimespan);
                    mListingsPresenter.updateSort(mSelectedSort, mSelectedTimespan);
                    getActivity().invalidateOptionsMenu();
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
                FlurryAgent.logEvent("option - change sort");
                return true;
            case R.id.action_change_timespan:
                showTimespanOptionsMenu();
                FlurryAgent.logEvent("option - change timespan");
                return true;
            case R.id.action_refresh:
                mListingsPresenter.refreshData();
                FlurryAgent.logEvent("option - refresh");
                return true;
            case R.id.action_settings:
                ((MainActivity) getActivity()).showSettings();
                FlurryAgent.logEvent("option - settings");
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void showSortOptionsMenu() {
        FragmentManager fm = getActivity().getFragmentManager();
        ChooseLinkSortDialog chooseLinkSortDialog = ChooseLinkSortDialog.newInstance(mListingsPresenter.getSort());
        chooseLinkSortDialog.setTargetFragment(this, REQUEST_CHOOSE_SORT);
        chooseLinkSortDialog.show(fm, DIALOG_CHOOSE_SORT);
    }

    void showTimespanOptionsMenu() {
        FragmentManager fm = getActivity().getFragmentManager();
        ChooseTimespanDialog chooseTimespanDialog = ChooseTimespanDialog.newInstance(mListingsPresenter.getTimespan());
        chooseTimespanDialog.setTargetFragment(this, REQUEST_CHOOSE_TIMESPAN);
        chooseTimespanDialog.show(fm, DIALOG_CHOOSE_TIMESPAN);
    }

    @Override
    public void showLinkContextMenu(ContextMenu menu, View v, Link link) {
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
    public void showCommentContextMenu(ContextMenu menu, View v, Comment comment) {
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
            case R.id.action_comment_permalink:
                mListingsPresenter.openCommentPermalink();
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
    public void openShareView(Link link) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, "http://www.reddit.com" + link.getPermalink());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public void openLinkInBrowser(Link link) {
        Uri uri = Uri.parse(link.getUrl());
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public void openCommentsInBrowser(Link link) {
        Uri uri = Uri.parse("http://www.reddit.com" + link.getPermalink());
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public void openLinkInWebView(Link link) {
        // Log analytics event
        Map<String, String> params = new HashMap<>();
        params.put("subreddit", link.getSubreddit());
        params.put("id", link.getId());
        params.put("domain", link.getDomain());
        params.put("created", new Date(Double.valueOf(link.getCreatedUtc() * 1000).longValue()).toString());
        params.put("nsfw", String.valueOf(link.getOver18()));
        params.put("score", String.valueOf(link.getScore()));
        FlurryAgent.logEvent("open link", params);

        ((MainActivity) getActivity()).showWebViewForURL(link.getUrl());
    }

    @Override
    public void showCommentsForLink(String subreddit, String linkId, String commentId) {
        Fragment fragment = LinkCommentsFragment.newInstance(subreddit, linkId, commentId);
        FragmentManager fm = getActivity().getFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void showCommentThread(String subreddit, String linkId, String commentId) {
        showCommentsForLink(subreddit, linkId, commentId);
    }

    @Override
    public void openReplyView(Comment comment) {
        showToast(R.string.implementation_pending);
    }

    @Override
    public void openShareView(Comment comment) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, comment.getUrl());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public void openUserProfileView(Link link) {
        ((MainView) getActivity()).showUserProfile("overview", link.getAuthor());
    }

    @Override
    public void openUserProfileView(Comment comment) {
        ((MainView) getActivity()).showUserProfile("overview", comment.getAuthor());
    }

    @Override
    public void openCommentInBrowser(Comment comment) {
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
