package com.ddiehl.android.simpleredditreader.view;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.events.ListingsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.LoadHotListingsEvent;
import com.ddiehl.android.simpleredditreader.redditapi.listings.RedditListingData;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ListingFragment extends ListFragment {
    private static final String TAG = ListingFragment.class.getSimpleName();

    private static final String ARG_SUBREDDIT = "subreddit";

    private Bus mBus;
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

        Bundle args = getArguments();
        mSubreddit = args.getString(ARG_SUBREDDIT);

        mData = new ArrayList<>();
        setListAdapter(new ListingAdapter(mData));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        getActivity().setTitle("/r/" + mSubreddit);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getBus().register(this);

        if (!mListingsRetrieved) {
            getBus().post(new LoadHotListingsEvent(mSubreddit)); // Load Hot listings from subreddit
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getBus().unregister(this);
    }

    @Subscribe
    public void onHotListingsLoaded(ListingsLoadedEvent event) {
        Log.d(TAG, "Listings loaded: " + event.getListings().size());
        mListingsRetrieved = true;

        mData.clear();
        mData.addAll(event.getListings());
        ((ListingAdapter) getListAdapter()).notifyDataSetChanged();
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

            String createDateFormatted = new Date(link.getCreatedUtc().longValue()).toString();

            ((TextView) view.findViewById(R.id.listing_score)).setText(String.valueOf(link.getScore()));
            ((TextView) view.findViewById(R.id.listing_title)).setText(link.getTitle());
            ((TextView) view.findViewById(R.id.listing_author)).setText(link.getAuthor());
            ((TextView) view.findViewById(R.id.listing_subreddit)).setText(link.getSubreddit());
            ((TextView) view.findViewById(R.id.listing_timestamp)).setText(createDateFormatted);

            return view;
        }
    }
}
