package com.ddiehl.reddit.listings;

import com.google.gson.annotations.Expose;

import java.util.List;

public class ListingResponseData {

    private String modhash;
    @Expose private List<Listing> children;
    @Expose private String after;
    private String before;

    public String getModhash() {
        return modhash;
    }

    public List getChildren() {
        return children;
    }

    public String getAfter() {
        return after;
    }

    public String getBefore() {
        return before;
    }
}
