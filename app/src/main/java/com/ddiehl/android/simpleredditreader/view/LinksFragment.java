package com.ddiehl.android.simpleredditreader.view;

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
import android.widget.Toast;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.model.listings.RedditLink;
import com.ddiehl.android.simpleredditreader.presenter.LinksPresenter;
import com.ddiehl.android.simpleredditreader.presenter.LinksPresenterImpl;
import com.squareup.otto.Bus;

public class LinksFragment extends Fragment implements LinksView {
    private static final String TAG = LinksFragment.class.getSimpleName();

    private static final String ARG_SUBREDDIT = "subreddit";

    private static final int REQUEST_CHOOSE_SORT = 0;
    private static final int REQUEST_CHOOSE_TIMESPAN = 1;
    private static final String DIALOG_CHOOSE_SORT = "dialog_choose_sort";
    private static final String DIALOG_CHOOSE_TIMESPAN = "dialog_choose_timespan";

    private Bus mBus;

    private LinksPresenter mLinksPresenter;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private LinksAdapter mLinksAdapter;

    private String mLastDisplayedLink;
    private boolean mLinksRequested = false;
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
        mLinksAdapter = new LinksAdapter(mLinksPresenter, this);

        mLinksPresenter.updateTitle();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.links_fragment, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mLinksAdapter);

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                mVisibleItemCount = mLayoutManager.getChildCount();
                mTotalItemCount = mLayoutManager.getItemCount();
                mFirstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                if (!mLinksRequested) {
                    if ((mVisibleItemCount + mFirstVisibleItem) >= mTotalItemCount) {
                        getLinks();
                    }
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

        if (mLastDisplayedLink == null) {
            getLinks();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mBus.unregister(mLinksPresenter);
    }

    private void getLinks() {
        mLinksRequested = true;
        mLinksPresenter.getLinks();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHOOSE_SORT:
                if (resultCode == Activity.RESULT_OK) {
                    String selectedSort = data.getStringExtra(ChooseLinkSortDialog.EXTRA_SORT);
                    mLinksPresenter.updateSort(selectedSort);
                }
                getActivity().supportInvalidateOptionsMenu();
                break;
            case REQUEST_CHOOSE_TIMESPAN:
                if (resultCode == Activity.RESULT_OK) {
                    String selectedTimespan = data.getStringExtra(ChooseTimespanDialog.EXTRA_TIMESPAN);
                    mLinksPresenter.updateTimeSpan(selectedTimespan);
                }
                getActivity().supportInvalidateOptionsMenu();
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
        } else { // controversial, rising
            menu.findItem(R.id.action_change_timespan).setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        switch (item.getItemId()) {
            case R.id.action_change_sort:
                ChooseLinkSortDialog chooseLinkSortDialog = ChooseLinkSortDialog.newInstance(null);
                chooseLinkSortDialog.setTargetFragment(this, REQUEST_CHOOSE_SORT);
                chooseLinkSortDialog.show(fm, DIALOG_CHOOSE_SORT);
                return true;
            case R.id.action_change_timespan:
                ChooseTimespanDialog chooseTimespanDialog = ChooseTimespanDialog.newInstance(null);
                chooseTimespanDialog.setTargetFragment(this, REQUEST_CHOOSE_TIMESPAN);
                chooseTimespanDialog.show(fm, DIALOG_CHOOSE_TIMESPAN);
                return true;
            case R.id.action_refresh:
                getLinks();
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showLinkContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.link_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return mLinksPresenter.onContextItemSelected(item);
    }

    @Override
    public void openIntent(Intent i) {
        startActivity(i);
    }

    @Override
    public void setTitle(String title) {
        getActivity().setTitle(title);
    }

    @Override
    public void showSpinner(String msg) {
        ((MainActivity) getActivity()).showSpinner(msg);
    }

    @Override
    public void dismissSpinner() {
        ((MainActivity) getActivity()).dismissSpinner();
    }

    @Override
    public void showToast(int resId) {
        Toast.makeText(getActivity(), resId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLink(Uri uri) {
        ((MainActivity) getActivity()).openWebViewForURL(uri.toString());
    }

    @Override
    public void showCommentsForLink(RedditLink link) {
        String subreddit = link.getSubreddit();
        String articleId = link.getId();

        Fragment fragment = LinkCommentsFragment.newInstance(subreddit, articleId);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void updateSubreddit(String subreddit) {
        mLinksPresenter.updateSubreddit(subreddit);
    }

    @Override
    public void updateAdapter() {
        mLinksAdapter.notifyDataSetChanged();
    }
}
