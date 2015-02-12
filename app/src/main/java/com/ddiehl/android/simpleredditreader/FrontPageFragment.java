package com.ddiehl.android.simpleredditreader;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.redditapi.listings.RedditListingData;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class FrontPageFragment extends ListFragment {
    private static final String TAG = FrontPageFragment.class.getSimpleName();

    private Bus mBus;
    private List<RedditListingData> mData;
    private boolean mListingsRetrieved = false;

    public FrontPageFragment() { /* Empty constructor required */ }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mData = new ArrayList<>();
        setListAdapter(new ListingAdapter(mData));
    }

    @Override
    public void onResume() {
        super.onResume();
        getBus().register(this);

        if (!mListingsRetrieved) {
            Log.d(TAG, "Loading listings...");
            getBus().post(new LoadHotListingsEvent()); // Load Hot listings from /r/all
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getBus().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        return v;
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
//                view = getActivity().getLayoutInflater().inflate(R.layout.listing_item, parent, false);
                view = getActivity().getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
            }

            RedditListingData link = getItem(position);

//            ((TextView) view.findViewById(R.id.item_text)).setText(link.getTitle());
            ((TextView) view).setText(link.getTitle());

            return view;
        }
    }
}
