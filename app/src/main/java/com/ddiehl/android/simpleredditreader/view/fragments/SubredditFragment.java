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

import com.ddiehl.android.simpleredditreader.BusProvider;
import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.presenter.ListingPresenter;
import com.ddiehl.android.simpleredditreader.presenter.AbsListingPresenter;
import com.ddiehl.android.simpleredditreader.presenter.SubredditPresenter;
import com.ddiehl.android.simpleredditreader.view.LinksView;
import com.ddiehl.android.simpleredditreader.view.SettingsChangedListener;
import com.ddiehl.android.simpleredditreader.view.activities.MainActivity;
import com.ddiehl.android.simpleredditreader.view.adapters.ListingAdapter;
import com.ddiehl.android.simpleredditreader.view.dialogs.ChooseLinkSortDialog;
import com.ddiehl.android.simpleredditreader.view.dialogs.ChooseTimespanDialog;
import com.ddiehl.reddit.listings.RedditLink;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import retrofit.RetrofitError;

public class SubredditFragment extends AbsRedditFragment
        implements LinksView, SettingsChangedListener {
    private static final String TAG = SubredditFragment.class.getSimpleName();

    private static final String ARG_SUBREDDIT = "subreddit";

    private static final int REQUEST_CHOOSE_SORT = 0;
    private static final int REQUEST_CHOOSE_TIMESPAN = 1;
    private static final String DIALOG_CHOOSE_SORT = "dialog_choose_sort";
    private static final String DIALOG_CHOOSE_TIMESPAN = "dialog_choose_timespan";

    private Bus mBus = BusProvider.getInstance();
    private ListingPresenter mListingPresenter;
    private ListingAdapter mListingAdapter;

    private int mFirstVisibleItem, mVisibleItemCount, mTotalItemCount;

    public SubredditFragment() { }

    public static SubredditFragment newInstance(String subreddit) {
        Bundle args = new Bundle();
        args.putString(ARG_SUBREDDIT, subreddit);
        SubredditFragment fragment = new SubredditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        mBus = BusProvider.getInstance();

        Bundle args = getArguments();
        String subreddit = args.getString(ARG_SUBREDDIT);
        mListingPresenter = new SubredditPresenter(getActivity(), this, subreddit);
        mListingAdapter = new ListingAdapter(mListingPresenter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.links_fragment, container, false);

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        RecyclerView rv = ButterKnife.findById(v, R.id.recycler_view);
        rv.setLayoutManager(mLayoutManager);
        rv.setAdapter(mListingAdapter);

        rv.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                mVisibleItemCount = mLayoutManager.getChildCount();
                mTotalItemCount = mLayoutManager.getItemCount();
                mFirstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                if ((mVisibleItemCount + mFirstVisibleItem) >= mTotalItemCount) {
                    mListingPresenter.getMoreData();
                }
            }
        });

        updateTitle();

        return v;
    }

    private void updateTitle() {
        String subreddit = mListingPresenter.getSubreddit();
        getActivity().setTitle(subreddit == null ?
                getString(R.string.front_page_title) :
                String.format(getString(R.string.link_subreddit), subreddit));
    }

    @Override
    public void onResume() {
        super.onResume();
        mBus.register(mListingPresenter);

        if (mListingAdapter.getItemCount() == 0) {
            mListingPresenter.refreshData();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mBus.unregister(mListingPresenter);
    }

    private String mSelectedSort, mSelectedTimespan;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHOOSE_SORT:
                if (resultCode == Activity.RESULT_OK) {
                    mSelectedSort = data.getStringExtra(ChooseLinkSortDialog.EXTRA_SORT);
                    if (mSelectedSort.equals("top") || mSelectedSort.equals("controversial")) {
                        showLinkTimespanOptionsMenu();
                    } else {
                        mListingPresenter.updateSort(mSelectedSort, mSelectedTimespan);
                        getActivity().supportInvalidateOptionsMenu();
                    }
                }
                break;
            case REQUEST_CHOOSE_TIMESPAN:
                if (resultCode == Activity.RESULT_OK) {
                    mSelectedTimespan = data.getStringExtra(ChooseTimespanDialog.EXTRA_TIMESPAN);
                    mListingPresenter.updateSort(mSelectedSort, mSelectedTimespan);
                    getActivity().supportInvalidateOptionsMenu();
                }
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.links_menu, menu);

        // Disable timespan option if current sort does not support it
        String sort = mListingPresenter.getSort();
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
                showLinkSortOptionsMenu();
                return true;
            case R.id.action_change_timespan:
                showLinkTimespanOptionsMenu();
                return true;
            case R.id.action_refresh:
                mListingPresenter.getLinks();
                return true;
            case R.id.action_settings:
                ((MainActivity) getActivity()).showSettings();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLinkSortOptionsMenu() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        ChooseLinkSortDialog chooseLinkSortDialog = ChooseLinkSortDialog.newInstance(mListingPresenter.getSort());
        chooseLinkSortDialog.setTargetFragment(this, REQUEST_CHOOSE_SORT);
        chooseLinkSortDialog.show(fm, DIALOG_CHOOSE_SORT);
    }

    private void showLinkTimespanOptionsMenu() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        ChooseTimespanDialog chooseTimespanDialog = ChooseTimespanDialog.newInstance(mListingPresenter.getTimespan());
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
                mListingPresenter.upvote();
                return true;
            case R.id.action_link_downvote:
                mListingPresenter.downvote();
                return true;
            case R.id.action_link_show_comments:
                mListingPresenter.showCommentsForLink();
                return true;
            case R.id.action_link_save:
                mListingPresenter.saveLink();
                return true;
            case R.id.action_link_unsave:
                mListingPresenter.unsaveLink();
                return true;
            case R.id.action_link_share:
                mListingPresenter.shareLink();
                return true;
            case R.id.action_link_open_in_browser:
                mListingPresenter.openLinkInBrowser();
                return true;
            case R.id.action_link_open_comments_in_browser:
                mListingPresenter.openCommentsInBrowser();
                return true;
            case R.id.action_link_hide:
                mListingPresenter.hideLink();
                return true;
            case R.id.action_link_unhide:
                mListingPresenter.unhideLink();
                return true;
            case R.id.action_link_report:
                mListingPresenter.reportLink();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void linksUpdated() {
        mListingAdapter.notifyDataSetChanged();
    }

    @Override
    public void linkUpdatedAt(int position) {
        mListingAdapter.notifyItemChanged(position);
    }

    @Override
    public void linkRemovedAt(int position) {
        mListingAdapter.notifyItemRemoved(position);
    }

    @Override
    public void openWebViewForLink(RedditLink link) {
        ((MainActivity) getActivity()).openWebViewForURL(link.getUrl());
    }

    @Override
    public void showCommentsForLink(RedditLink link) {
        String subreddit = link.getSubreddit();
        String articleId = link.getId();

        Fragment fragment = LinkCommentsFragment.newInstance(subreddit, articleId, null);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void updateSubreddit(String subreddit) {
        mListingPresenter.updateSubreddit(subreddit);
    }

    @Subscribe
    public void onError(RetrofitError error) {
        dismissSpinner();
    }

    @Override
    public void onSettingsChanged() {
        // No views dependent on settings
    }
}
