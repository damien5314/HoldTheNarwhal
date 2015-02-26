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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
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

public class LinksFragment extends Fragment {
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
    private String mLastDisplayedLink;
    private boolean mLinksRequested = false;
    private int mFirstVisibleItem, mVisibleItemCount, mTotalItemCount;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private LinkAdapter mLinkAdapter;
    private SingleSelector mSelector;
    private ProgressDialog mProgressBar;

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
        updateTitle();

        mData = new ArrayList<>();
        mLinkAdapter = new LinkAdapter();
        mSelector = new SingleSelector();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.links_fragment, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mLinkAdapter);

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                mVisibleItemCount = mLayoutManager.getChildCount();
                mTotalItemCount = mLayoutManager.getItemCount();
                mFirstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                if (!mLinksRequested) {
                    if ( (mVisibleItemCount + mFirstVisibleItem) >= mTotalItemCount) {
                        mLinksRequested = true;
                        getBus().post(new LoadLinksEvent(mSubreddit, mSort, mTimeSpan, mLastDisplayedLink));
                    }
                }
            }
        });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // Register view for context menu
            registerForContextMenu(mRecyclerView);
        }

        return v;
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

    @TargetApi(11)
    private void openLinkContextMenu(final RedditLink link) {
        getActivity().startActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.link_context_menu, menu);
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
                        upvote(link);
                        return true;
                    case R.id.action_downvote:
                        mode.finish();
                        downvote(link);
                        return true;
                    case R.id.action_show_comments:
                        mode.finish();
                        openCommentsForLink(link);
                        return true;
                    case R.id.action_save:
                        mode.finish();
                        saveLink(link);
                        return true;
                    case R.id.action_share:
                        mode.finish();
                        shareLink(link);
                        return true;
                    case R.id.action_open_in_browser:
                        mode.finish();
                        openLinkInBrowser(link);
                        return true;
                    case R.id.action_open_comments_in_browser:
                        mode.finish();
                        openCommentsInBrowser(link);
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                int selectedPosition = mSelector.getSelectedPosition();
                mSelector.setItemSelected(selectedPosition, false);
                mLinkAdapter.notifyItemChanged(selectedPosition);
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
        updateTitle();
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

    private void saveLink(RedditLink link) {

    }

    private void shareLink(RedditLink link) {
        String url = "http://www.reddit.com" + link.getPermalink();
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, url);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    private void openLinkInBrowser(RedditLink link) {
        Uri uri = Uri.parse(link.getUrl());
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    private void openCommentsInBrowser(RedditLink link) {
        Uri uri = Uri.parse("http://www.reddit.com" + link.getPermalink());
        Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
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
                    String selectedSort = data.getStringExtra(ChooseLinkSortDialog.EXTRA_SORT);
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
        getActivity().getMenuInflater().inflate(R.menu.link_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        RedditLink link = mData.get(position);

        switch (item.getItemId()) {
            // Set actions for each menu item
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.links_menu, menu);

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
                ChooseLinkSortDialog chooseLinkSortDialog = ChooseLinkSortDialog.newInstance(mSort);
                chooseLinkSortDialog.setTargetFragment(this, REQUEST_CHOOSE_SORT);
                chooseLinkSortDialog.show(fm, DIALOG_CHOOSE_SORT);
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

    private class LinkAdapter extends RecyclerView.Adapter<LinkHolder> {
        @Override
        public LinkHolder onCreateViewHolder(ViewGroup parent, int i) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.reddit_link_item, parent, false);
            return new LinkHolder(view);
        }

        @Override
        public void onBindViewHolder(LinkHolder linkHolder, int i) {
            RedditLink link = mData.get(i);
            linkHolder.bindLink(link);
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

    private class LinkHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private RedditLink mRedditLink;
        private TextView mScoreView;
        private TextView mTitleView;
        private TextView mAuthorView;
        private TextView mTimestampView;
        private TextView mSubredditView;
        private TextView mDomainView;
        private ImageView mThumbnailView;

        public LinkHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mScoreView = (TextView) itemView.findViewById(R.id.link_score);
            mTitleView = (TextView) itemView.findViewById(R.id.link_title);
            mAuthorView = (TextView) itemView.findViewById(R.id.link_author);
            mTimestampView = (TextView) itemView.findViewById(R.id.link_timestamp);
            mSubredditView = (TextView) itemView.findViewById(R.id.link_subreddit);
            mDomainView = (TextView) itemView.findViewById(R.id.link_domain);
            mThumbnailView = (ImageView) itemView.findViewById(R.id.link_thumbnail);
        }

        public void bindLink(RedditLink link) {
            mRedditLink = link;

            String createDateFormatted = Utils.getFormattedDateStringFromUtc(link.getCreatedUtc().longValue());

            // Set content for each TextView
            mScoreView.setText(String.valueOf(link.getScore()));
            mTitleView.setText(link.getTitle());
            mAuthorView.setText("/u/" + link.getAuthor());
            mTimestampView.setText(createDateFormatted);
            mSubredditView.setText("/r/" + link.getSubreddit());
            mDomainView.setText("(" + link.getDomain() + ")");

            // Register context menu to click event for score view
            mScoreView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {

                    } else { // Activate contextual action bar
                        openLinkContextMenu(mRedditLink);
                        int pos = getPosition();
                        mSelector.setItemSelected(pos, true);
                        mLinkAdapter.notifyItemChanged(pos);
                    }
                }
            });

            // Queue thumbnail to be downloaded, if one exists
            mThumbnailView.setImageResource(R.drawable.ic_thumbnail_placeholder);

            String thumbnailUrl = link.getThumbnail();
            if (thumbnailUrl.equals("nsfw")) {
                mThumbnailView.setImageResource(R.drawable.ic_nsfw);
            } else if (!thumbnailUrl.equals("")
                    && !thumbnailUrl.equals("default")
                    && !thumbnailUrl.equals("self")) {
                Bitmap thumbnail = mThumbnailCache.getThumbnail(thumbnailUrl);
                if (thumbnail == null) {
                    mThumbnailThread.queueThumbnail(mThumbnailView, thumbnailUrl);
                } else {
                    mThumbnailView.setImageBitmap(thumbnail);
                }
                mThumbnailView.setVisibility(View.VISIBLE);
            } else {
                mThumbnailView.setVisibility(View.GONE);
            }

            boolean isSelected = mSelector.isItemSelected(getPosition());
            itemView.setActivated(isSelected);
        }

        @Override
        public void onClick(View v) {
            if (mRedditLink != null) {
                if (mRedditLink.isSelf()) {
                    openCommentsForLink(mRedditLink);
                } else {
                    Uri webViewUri = Uri.parse(mRedditLink.getUrl());
                    Intent i = new Intent(getActivity(), WebViewActivity.class);
                    i.setData(webViewUri);
                    startActivity(i);
                }
            }
        }
    }
}
