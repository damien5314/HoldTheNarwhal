package com.ddiehl.android.simpleredditreader.redditapi.listings;

import com.google.gson.annotations.Expose;

/**
 * Created by Damien on 2/5/2015.
 */
public class RedditListing {

    @Expose
    private String kind;
    @Expose
    private RedditListingData data;

    /**
     *
     * @return
     * The kind
     */
    public String getKind() {
        return kind;
    }

    /**
     *
     * @param kind
     * The kind
     */
    public void setKind(String kind) {
        this.kind = kind;
    }

    /**
     *
     * @return
     * The data
     */
    public RedditListingData getData() {
        return data;
    }

    /**
     *
     * @param data
     * The data
     */
    public void setData(RedditListingData data) {
        this.data = data;
    }

}