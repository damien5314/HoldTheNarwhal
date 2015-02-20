package com.ddiehl.android.simpleredditreader.model.listings;

import com.google.gson.annotations.Expose;

public class ListingResponse<T extends Listing> {

    @Expose
    private String kind;
    @Expose
    private ListingResponseData<T> data;


    public String getKind() {
        return kind;
    }

    public ListingResponseData<T> getData() {
        return data;
    }

}
