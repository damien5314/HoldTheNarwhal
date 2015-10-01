package com.ddiehl.android.htn.events.responses;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.TrophyResponse;

import java.util.List;


public class TrophiesLoadedEvent extends FailableEvent {
    private TrophyResponse mResponse;
    private List<Listing> mListings;

    public TrophiesLoadedEvent(@NonNull TrophyResponse response) {
        mResponse = response;
        mListings = response.getData().getTrophies();
    }

    public TrophiesLoadedEvent(@NonNull Throwable e) {
        super(e);
    }

    @Nullable
    public TrophyResponse getResponse() {
        return mResponse;
    }

    @Nullable
    public List<Listing> getListings() {
        return mListings;
    }
}
