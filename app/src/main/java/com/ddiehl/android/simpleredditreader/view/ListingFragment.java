package com.ddiehl.android.simpleredditreader.view;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.Utils;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.events.ListingsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.LoadHotListingsEvent;
import com.ddiehl.android.simpleredditreader.events.RandomSubredditLoadedEvent;
import com.ddiehl.android.simpleredditreader.redditapi.listings.RedditListingData;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class ListingFragment extends ListFragment {
    private static final String TAG = ListingFragment.class.getSimpleName();

    private static final String ARG_SUBREDDIT = "subreddit";

    private Bus mBus;
    private ThumbnailDownloader<ImageView> mThumbnailThread;
    private ThumbnailCache mThumbnailCache;

    private String mSubreddit;
    private List<RedditListingData> mData;
    private boolean mListingsRetrieved = false;

    public ListingFragment() { /* Default constructor required */ }

    public static ListingFragment newInstance(String subreddit) {
        Bundle args = new Bundle();
        args.putString(ARG_SUBREDDIT, subreddit);
        ListingFragment fragment = new ListingFragment();
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
        setListAdapter(new ListingAdapter(mData));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.listings_fragment, null);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getBus().register(this);

        getActivity().setTitle("/r/" + mSubreddit);

        if (!mListingsRetrieved) {
            getBus().post(new LoadHotListingsEvent(mSubreddit)); // Load Hot listings from subreddit
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

    @Subscribe
    public void onListingsLoaded(ListingsLoadedEvent event) {
        mListingsRetrieved = true;

        mData.clear();
        mData.addAll(event.getListings());
        ((ListingAdapter) getListAdapter()).notifyDataSetChanged();
    }

    @Subscribe
    public void onRandomSubredditLoaded(RandomSubredditLoadedEvent event) {
        mListingsRetrieved = true;

        mData.clear();
        mData.addAll(event.getListings());
        ((ListingAdapter) getListAdapter()).notifyDataSetChanged();

        getActivity().setTitle("/r/" + mData.get(0).getSubreddit());
    }

    private Bus getBus() {
        if (mBus == null) {
            mBus = BusProvider.getInstance();
        }
        return mBus;
    }

    private class ListingAdapter extends ArrayAdapter<RedditListingData> {
        public ListingAdapter(List<RedditListingData> data) {
            super(getActivity(), 0, data);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.listing_item, null);
            }

            RedditListingData link = getItem(position);

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
