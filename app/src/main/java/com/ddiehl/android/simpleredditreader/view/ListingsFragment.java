package com.ddiehl.android.simpleredditreader.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.Utils;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.events.LinksLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.LoadHotLinksEvent;
import com.ddiehl.android.simpleredditreader.redditapi.listings.RedditLink;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class ListingsFragment extends ListFragment {
    private static final String TAG = ListingsFragment.class.getSimpleName();

    private static final String ARG_SUBREDDIT = "subreddit";

    private Bus mBus;
    private ThumbnailDownloader<ImageView> mThumbnailThread;
    private ThumbnailCache mThumbnailCache;

    private String mSubreddit;
    private List<RedditLink> mData;
    private ListingAdapter mListingAdapter;
    private boolean mListingsRetrieved = false;

    public ListingsFragment() { /* Default constructor required */ }

    public static ListingsFragment newInstance(String subreddit) {
        Bundle args = new Bundle();
        args.putString(ARG_SUBREDDIT, subreddit);
        ListingsFragment fragment = new ListingsFragment();
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
        mListingAdapter = new ListingAdapter(mData);
        setListAdapter(mListingAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText(getString(R.string.no_listings_found));
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

        if (!mListingsRetrieved) {
            setListShown(false);
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
        mListingsRetrieved = true;

        mData.clear();
        mData.addAll(event.getLinks());
        mListingAdapter.notifyDataSetChanged();
        setListShown(true);
    }

    private Bus getBus() {
        if (mBus == null) {
            mBus = BusProvider.getInstance();
        }
        return mBus;
    }

    private class ListingAdapter extends ArrayAdapter<RedditLink> {
        public ListingAdapter(List<RedditLink> data) {
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
