package com.ddiehl.android.simpleredditreader.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.Utils;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.events.LinksLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.LoadHotLinksEvent;
import com.ddiehl.android.simpleredditreader.model.listings.RedditLink;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class LinksFragment extends ListFragment {
    private static final String TAG = LinksFragment.class.getSimpleName();

    private static final String ARG_SUBREDDIT = "subreddit";

    private Bus mBus;
    private ThumbnailDownloader<ImageView> mThumbnailThread;
    private ThumbnailCache mThumbnailCache;

    private String mSubreddit;
    private List<RedditLink> mData;
    private LinkAdapter mLinkAdapter;
    private String mLastDisplayedLink;
    private boolean mLinksRequested = false;

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

        mData = new ArrayList<>();
        mLinkAdapter = new LinkAdapter(mData);
        setListAdapter(mLinkAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText(getString(R.string.no_listings_found));

        ListView listView = getListView();

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override public void onScrollStateChanged(AbsListView view, int scrollState) { }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if ((firstVisibleItem + visibleItemCount) == totalItemCount
                        && totalItemCount > 0
                        && !mLinksRequested) {
                    mLinksRequested = true;
                    getBus().post(new LoadHotLinksEvent(mSubreddit, mLastDisplayedLink));
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getBus().register(this);

        if (mSubreddit == null) {
            getActivity().setTitle(getString(R.string.front_page_title));
        } else {
            getActivity().setTitle("/r/" + mSubreddit);
        }

        if (mLastDisplayedLink == null) {
            setListShown(false);
            mLinksRequested = true;
            getBus().post(new LoadHotLinksEvent(mSubreddit));
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
    public void onListItemClick(ListView l, View v, int position, long id) {
        RedditLink listing = mData.get(position);
        String subreddit = listing.getSubreddit();
        String articleId = listing.getId();

        Intent i = new Intent(getActivity(), CommentsActivity.class);
        i.putExtra(CommentsActivity.EXTRA_SUBREDDIT, subreddit);
        i.putExtra(CommentsActivity.EXTRA_ARTICLE, articleId);
        startActivity(i);
    }

    @Subscribe
    public void onListingsLoaded(LinksLoadedEvent event) {
        mLinksRequested = false;
        mLastDisplayedLink = event.getResponse().getData().getAfter();

//        mData.clear();
        mData.addAll(event.getLinks());
        mLinkAdapter.notifyDataSetChanged();
        setListShown(true);
    }

    private Bus getBus() {
        if (mBus == null) {
            mBus = BusProvider.getInstance();
        }
        return mBus;
    }

    private class LinkAdapter extends ArrayAdapter<RedditLink> {
        public LinkAdapter(List<RedditLink> data) {
            super(getActivity(), 0, data);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.listings_item, null);
            }

            RedditLink link = getItem(position);

            String createDateFormatted = Utils.getFormattedDateStringFromUtc(link.getCreatedUtc().longValue());

            // Set content for each TextView
            ((TextView) view.findViewById(R.id.listing_score)).setText(String.valueOf(link.getScore()));
            ((TextView) view.findViewById(R.id.listing_title)).setText(link.getTitle());
            ((TextView) view.findViewById(R.id.listing_author)).setText("/u/" + link.getAuthor());
            ((TextView) view.findViewById(R.id.listing_timestamp)).setText(createDateFormatted);
            ((TextView) view.findViewById(R.id.listing_subreddit)).setText("/r/" + link.getSubreddit());
            ((TextView) view.findViewById(R.id.listing_domain)).setText("(" + link.getDomain() + ")");

            // Queue thumbnail to be downloaded, if one exists
            ImageView thumbnailImageView = (ImageView) view.findViewById(R.id.listing_thumbnail);
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
