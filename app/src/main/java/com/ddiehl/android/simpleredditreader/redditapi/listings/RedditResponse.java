package com.ddiehl.android.simpleredditreader.redditapi.listings;

import com.google.gson.annotations.Expose;

/**
 * Created by Damien on 2/5/2015.
 */
public class RedditResponse {

    @Expose
    private String kind;
    @Expose
    private RedditResponseData data;

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
    public RedditResponseData getData() {
        return data;
    }

    /**
     *
     * @param data
     * The data
     */
    public void setData(RedditResponseData data) {
        this.data = data;
    }

}
