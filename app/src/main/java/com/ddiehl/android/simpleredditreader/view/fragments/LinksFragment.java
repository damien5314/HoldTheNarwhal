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
import com.ddiehl.android.simpleredditreader.presenter.LinksPresenter;
import com.ddiehl.android.simpleredditreader.presenter.LinksPresenterImpl;
import com.ddiehl.android.simpleredditreader.view.LinksView;
import com.ddiehl.android.simpleredditreader.view.MainView;
import com.ddiehl.android.simpleredditreader.view.SettingsChangedListener;
import com.ddiehl.android.simpleredditreader.view.activities.MainActivity;
import com.ddiehl.android.simpleredditreader.view.adapters.LinksAdapter;
import com.ddiehl.android.simpleredditreader.view.dialogs.ChooseLinkSortDialog;
import com.ddiehl.android.simpleredditreader.view.dialogs.ChooseTimespanDialog;
import com.ddiehl.reddit.listings.RedditLink;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import retrofit.RetrofitError;

public class LinksFragment extends Fragment
        implements LinksView, SettingsChangedListener {
    private static final String TAG = LinksFragment.class.getSimpleName();

    private static final String ARG_SUBREDDIT = "subreddit";

    private static final int REQUEST_CHOOSE_SORT = 0;
    private static final int REQUEST_CHOOSE_TIMESPAN = 1;
    private static final String DIALOG_CHOOSE_SORT = "dialog_choose_sort";
    private static final String DIALOG_CHOOSE_TIMESPAN = "dialog_choose_timespan";

    private Bus mBus = BusProvider.getInstance();
    private LinksPresenter mLinksPresenter;

    private LinksAdapter mLinksAdapter;

    private int mFirstVisibleItem, mVisibleItemCount, mTotalItemCount;

    public LinksFragment() { /* Default constructor required */ }

    public static LinksFragment newInstance(String subreddit) {
        Bundle args = new Bundle();
        args.putString(ARG_SUBREDDIT, subreddit);
        LinksFragment fragment = new LinksFragment();
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
        mLinksPresenter = new LinksPresenterImpl(getActivity(), this, subreddit);
        mLinksAdapter = new LinksAdapter(mLinksPresenter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.links_fragment, container, false);

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        RecyclerView rv = ButterKnife.findById(v, R.id.recycler_view);
        rv.setLayoutManager(mLayoutManager);
        rv.setAdapter(mLinksAdapter);

        rv.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                mVisibleItemCount = mLayoutManager.getChildCount();
                mTotalItemCount = mLayoutManager.getItemCount();
                mFirstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                if ((mVisibleItemCount + mFirstVisibleItem) >= mTotalItemCount) {
                    mLinksPresenter.getMoreLinks();
                }
            }
        });

        mLinksPresenter.updateTitle();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mBus.register(mLinksPresenter);

        if (mLinksAdapter.getItemCount() == 0) {
            mLinksPresenter.getLinks();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mBus.unregister(mLinksPresenter);
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
                        mLinksPresenter.updateSort(mSelectedSort, mSelectedTimespan);
                        getActivity().supportInvalidateOptionsMenu();
                    }
                }
                break;
            case REQUEST_CHOOSE_TIMESPAN:
                if (resultCode == Activity.RESULT_OK) {
                    mSelectedTimespan = data.getStringExtra(ChooseTimespanDialog.EXTRA_TIMESPAN);
                    mLinksPresenter.updateSort(mSelectedSort, mSelectedTimespan);
                    getActivity().supportInvalidateOptionsMenu();
                }
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.links_menu, menu);

        // Disable timespan option if current sort does not support it
        String sort = mLinksPresenter.getSort();
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
                mLinksPresenter.getLinks();
                return true;
            case R.id.action_settings:
                ((MainActivity) getActivity()).showSettings();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLinkSortOptionsMenu() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        ChooseLinkSortDialog chooseLinkSortDialog = ChooseLinkSortDialog.newInstance(mLinksPresenter.getSort());
        chooseLinkSortDialog.setTargetFragment(this, REQUEST_CHOOSE_SORT);
        chooseLinkSortDialog.show(fm, DIALOG_CHOOSE_SORT);
    }

    private void showLinkTimespanOptionsMenu() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        ChooseTimespanDialog chooseTimespanDialog = ChooseTimespanDialog.newInstance(mLinksPresenter.getTimespan());
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
                mLinksPresenter.upvote();
                return true;
            case R.id.action_link_downvote:
                mLinksPresenter.downvote();
                return true;
            case R.id.action_link_show_comments:
                mLinksPresenter.showCommentsForLink();
                return true;
            case R.id.action_link_save:
                mLinksPresenter.saveLink();
                return true;
            case R.id.action_link_unsave:
                mLinksPresenter.unsaveLink();
                return true;
            case R.id.action_link_share:
                mLinksPresenter.shareLink();
                return true;
            case R.id.action_link_open_in_browser:
                mLinksPresenter.openLinkInBrowser();
                return true;
            case R.id.action_link_open_comments_in_browser:
                mLinksPresenter.openCommentsInBrowser();
                return true;
            case R.id.action_link_hide:
                mLinksPresenter.hideLink();
                return true;
            case R.id.action_link_unhide:
                mLinksPresenter.unhideLink();
                return true;
            case R.id.action_link_report:
                mLinksPresenter.reportLink();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        getActivity().setTitle(title);
    }

    @Override
    public void showSpinner(String msg) {
        ((MainView) getActivity()).showSpinner(msg);
    }

    @Override
    public void showSpinner(int resId) {
        ((MainView) getActivity()).showSpinner(resId);
    }

    @Override
    public void dismissSpinner() {
        ((MainView) getActivity()).dismissSpinner();
    }

    @Override
    public void showToast(String msg) {
        ((MainView) getActivity()).showToast(msg);
    }

    @Override
    public void showToast(int resId) {
        ((MainView) getActivity()).showToast(resId);
    }

    @Override
    public void linksUpdated() {
        mLinksAdapter.notifyDataSetChanged();
    }

    @Override
    public void linkUpdatedAt(int position) {
        mLinksAdapter.notifyItemChanged(position);
    }

    @Override
    public void linkRemovedAt(int position) {
        mLinksAdapter.notifyItemRemoved(position);
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
        mLinksPresenter.updateSubreddit(subreddit);
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
