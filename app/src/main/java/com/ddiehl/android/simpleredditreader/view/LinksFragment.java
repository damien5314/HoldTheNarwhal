package com.ddiehl.android.simpleredditreader.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.Utils;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.events.LinksLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.LoadLinksEvent;
import com.ddiehl.android.simpleredditreader.events.VoteEvent;
import com.ddiehl.android.simpleredditreader.events.VoteSubmittedEvent;
import com.ddiehl.android.simpleredditreader.model.listings.RedditLink;
import com.ddiehl.android.simpleredditreader.web.ThumbnailCache;
import com.ddiehl.android.simpleredditreader.web.ThumbnailDownloader;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import retrofit.client.Response;

public class LinksFragment extends ListFragment {
    private static final String TAG = LinksFragment.class.getSimpleName();

    private static final String ARG_SUBREDDIT = "subreddit";
    private static final String ARG_SORT = "sort";
    private static final String ARG_TIMESPAN = "timespan";

    private static final int REQUEST_CHOOSE_SORT = 0;
    private static final int REQUEST_CHOOSE_TIMESPAN = 1;
    private static final String DIALOG_CHOOSE_SORT = "dialog_choose_sort";
    private static final String DIALOG_CHOOSE_TIMESPAN = "dialog_choose_timespan";

    private Bus mBus;
    private ThumbnailDownloader<ImageView> mThumbnailThread;
    private ThumbnailCache mThumbnailCache;

    private String mSubreddit;
    private String mSort;
    private String mTimeSpan;
    private List<RedditLink> mData;
    private LinkAdapter mLinkAdapter;
    private String mLastDisplayedLink;
    private boolean mLinksRequested = false;

    private Toolbar mToolbar;
    private ProgressDialog mProgressBar;
    private ListView mListView;

    public LinksFragment() { /* Default constructor required */ }

    public static LinksFragment newInstance(String subreddit, String sort, String timespan) {
        Bundle args = new Bundle();
        args.putString(ARG_SUBREDDIT, subreddit);
        args.putString(ARG_SORT, sort);
        args.putString(ARG_TIMESPAN, timespan);
        LinksFragment fragment = new LinksFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        mThumbnailThread = new ThumbnailDownloader<>(new Handler());
        mThumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>() {
            @Override
            public void onThumbnailDownloaded(ImageView imageView, String url, Bitmap thumbnail) {
                if (isVisible()) {
                    mThumbnailCache.addThumbnail(url, thumbnail);
                    imageView.setImageBitmap(thumbnail);
                }
            }
        });
        mThumbnailThread.start();
        mThumbnailThread.getLooper();

        mThumbnailCache = ThumbnailCache.getInstance();

        Bundle args = getArguments();
        mSubreddit = args.getString(ARG_SUBREDDIT);
        mSort = args.getString(ARG_SORT);
        mTimeSpan = args.getString(ARG_TIMESPAN);

        mData = new ArrayList<>();
        mLinkAdapter = new LinkAdapter(mData);
        setListAdapter(mLinkAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.links_fragment, null);

        mListView = (ListView) v.findViewById(android.R.id.list);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // Register view for context menu
            registerForContextMenu(mListView);
        } else {
            // Set up contextual action bar
            mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView listView = getListView();
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if ((firstVisibleItem + visibleItemCount) == totalItemCount
                        && totalItemCount > 0
                        && !mLinksRequested) {
                    mLinksRequested = true;
                    getBus().post(new LoadLinksEvent(mSubreddit, mSort, mTimeSpan, mLastDisplayedLink));
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getBus().register(this);

        updateTitle();

        if (mLastDisplayedLink == null) {
            getLinks();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getBus().unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailThread.quit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailThread.clearQueue();
    }

    @Override
    public void onListItemClick(ListView l, View v, final int position, long id) {
        RedditLink link = mData.get(position);

        if (link.isSelf()) {
            openCommentsForLink(mData.get(position));
        } else {
            Uri webViewUri = Uri.parse(link.getUrl());
            Intent i = new Intent(getActivity(), WebViewActivity.class);
            i.setData(webViewUri);
            startActivity(i);
        }
    }

    @TargetApi(11)
    private void openListItemContext(final int position) {
        getActivity().startActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_link_context, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_upvote:
                        mode.finish();
                        upvote(mData.get(position));
                        return true;
                    case R.id.action_downvote:
                        mode.finish();
                        downvote(mData.get(position));
                        return true;
                    case R.id.action_show_comments:
                        mode.finish();
                        openCommentsForLink(mData.get(position));
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mListView.setItemChecked(position, false);
            }
        });
    }

    public void openCommentsForLink(RedditLink link) {
        String subreddit = link.getSubreddit();
        String articleId = link.getId();

        Fragment fragment = CommentsFragment.newInstance(subreddit, articleId);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void updateSubreddit(String subreddit) {
        mData.clear();
        mSubreddit = subreddit;
        getLinks();
    }

    public void updateSort(String sort) {
        mData.clear();
        mSort = sort;
        getLinks();
    }

    public void updateTimeSpan(String timespan) {
        mData.clear();
        mTimeSpan = timespan;
        getLinks();
    }

    private void updateTitle() {
        if (mSubreddit == null) {
            getActivity().setTitle(getString(R.string.front_page_title));
        } else {
            getActivity().setTitle("/r/" + mSubreddit);
        }
    }

    private void getLinks() {
        showSpinner();
        mLinksRequested = true;
        getBus().post(new LoadLinksEvent(mSubreddit, mSort, mTimeSpan));
    }

    private void upvote(RedditLink link) {
        int dir = (link.isLiked() == null || !link.isLiked()) ? 1 : 0;
        getBus().post(new VoteEvent(link.getId(), dir));
    }

    private void downvote(RedditLink link) {
        int dir = (link.isLiked() == null || link.isLiked()) ? -1 : 0;
        getBus().post(new VoteEvent(link.getId(), dir));
    }

    @Subscribe
    public void onLinksLoaded(LinksLoadedEvent event) {
        dismissSpinner();
        if (event.isFailed()) {
            Log.e(TAG, "Error loading links", event.getError());
            return;
        }

        mLinksRequested = false;
        mLastDisplayedLink = event.getResponse().getData().getAfter();

        if (mSubreddit != null && mSubreddit.equals("random")) {
            mSubreddit = event.getLinks().get(0).getSubreddit();
        }

        updateTitle();
        mData.addAll(event.getLinks());
        mLinkAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onVoteSubmitted(VoteSubmittedEvent event) {
        if (event.isFailed()) {
            Toast.makeText(getActivity(), R.string.vote_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        Log.i(TAG, "Vote submitted successfully");
        Response response = event.getResponse();
    }

    private Bus getBus() {
        if (mBus == null) {
            mBus = BusProvider.getInstance();
        }
        return mBus;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHOOSE_SORT:
                if (resultCode == Activity.RESULT_OK) {
                    String selectedSort = data.getStringExtra(ChooseSortDialog.EXTRA_SORT);
                    if (!mSort.equals(selectedSort)) {
                        mSort = selectedSort;
                        mData.clear();
                        getLinks();
                    }
                }
                getActivity().supportInvalidateOptionsMenu();
                break;
            case REQUEST_CHOOSE_TIMESPAN:
                if (resultCode == Activity.RESULT_OK) {
                    String selectedTimespan = data.getStringExtra(ChooseTimespanDialog.EXTRA_TIMESPAN);
                    if (!mTimeSpan.equals(selectedTimespan)) {
                        mTimeSpan = selectedTimespan;
                        mData.clear();
                        getLinks();
                    }
                }
                getActivity().supportInvalidateOptionsMenu();
                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.menu_link_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        LinkAdapter adapter = (LinkAdapter) getListAdapter();
        RedditLink link = adapter.getItem(position);

        switch (item.getItemId()) {
            // Set actions for each menu item
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_links, menu);

        // Disable timespan option if current sort does not support it
        if (mSort.equals("hot") ||
                mSort.equals("new") ||
                mSort.equals("rising")) {
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
                ChooseSortDialog chooseSortDialog = ChooseSortDialog.newInstance(mSort);
                chooseSortDialog.setTargetFragment(this, REQUEST_CHOOSE_SORT);
                chooseSortDialog.show(fm, DIALOG_CHOOSE_SORT);
                return true;
            case R.id.action_change_timespan:
                ChooseTimespanDialog chooseTimespanDialog = ChooseTimespanDialog.newInstance(mTimeSpan);
                chooseTimespanDialog.setTargetFragment(this, REQUEST_CHOOSE_TIMESPAN);
                chooseTimespanDialog.show(fm, DIALOG_CHOOSE_TIMESPAN);
                return true;
            case R.id.action_refresh:
                mData.clear();
                getLinks();
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSpinner() {
        if (mProgressBar == null) {
            mProgressBar = new ProgressDialog(getActivity(), R.style.ProgressDialog);
            mProgressBar.setCancelable(false);
            mProgressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        mProgressBar.show();
    }

    private void dismissSpinner() {
        mProgressBar.dismiss();
    }

    private class LinkAdapter extends ArrayAdapter<RedditLink> {
        public LinkAdapter(List<RedditLink> data) {
            super(getActivity(), 0, data);
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.links_list_item, null);
            }

            RedditLink link = getItem(position);

            String createDateFormatted = Utils.getFormattedDateStringFromUtc(link.getCreatedUtc().longValue());

            // Set content for each TextView
            TextView vScore = (TextView) view.findViewById(R.id.link_score);
            vScore.setText(String.valueOf(link.getScore()));
            TextView vTitle = (TextView) view.findViewById(R.id.link_title);
            vTitle.setText(link.getTitle());
            TextView vAuthor = (TextView) view.findViewById(R.id.link_author);
            vAuthor.setText("/u/" + link.getAuthor());
            TextView vTimestamp = (TextView) view.findViewById(R.id.link_timestamp);
            vTimestamp.setText(createDateFormatted);
            TextView vSubreddit = (TextView) view.findViewById(R.id.link_subreddit);
            vSubreddit.setText("/r/" + link.getSubreddit());
            TextView vDomain = (TextView) view.findViewById(R.id.link_domain);
            vDomain.setText("(" + link.getDomain() + ")");

            // Register context menu to click event for score view
            vScore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {

                    } else { // Activate contextual action bar
                        mListView.setItemChecked(position, true);
                        openListItemContext(position);
                    }
                }
            });

            // Queue thumbnail to be downloaded, if one exists
            ImageView thumbnailImageView = (ImageView) view.findViewById(R.id.link_thumbnail);
            String thumbnailUrl = link.getThumbnail();
            if (thumbnailUrl.equals("nsfw")) {
                thumbnailImageView.setImageResource(R.drawable.ic_nsfw);
            } else if (!thumbnailUrl.equals("")
                    && !thumbnailUrl.equals("default")
                    && !thumbnailUrl.equals("self")) {
                Bitmap thumbnail = mThumbnailCache.getThumbnail(thumbnailUrl);
                if (thumbnail == null) {
                    mThumbnailThread.queueThumbnail(thumbnailImageView, thumbnailUrl);
                } else {
                    thumbnailImageView.setImageBitmap(thumbnail);
                }
                thumbnailImageView.setVisibility(View.VISIBLE);
            } else {
                thumbnailImageView.setVisibility(View.GONE);
            }

            return view;
        }
    }
}
