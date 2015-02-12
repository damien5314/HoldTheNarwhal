package com.ddiehl.android.simpleredditreader.redditapi.listings;

/**
 * Created by Damien on 2/5/2015.
 */
import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class ListingResponseData {

    @Expose
    private String modhash;
    @Expose
    private List<RedditListing> children = new ArrayList<RedditListing>();
    @Expose
    private String after;
    @Expose
    private Object before;

    /**
     *
     * @return
     * The modhash
     */
    public String getModhash() {
        return modhash;
    }

    /**
     *
     * @param modhash
     * The modhash
     */
    public void setModhash(String modhash) {
        this.modhash = modhash;
    }

    /**
     *
     * @return
     * The children
     */
    public List<RedditListing> getChildren() {
        return children;
    }

    /**
     *
     * @param children
     * The children
     */
    public void setChildren(List<RedditListing> children) {
        this.children = children;
    }

    /**
     *
     * @return
     * The after
     */
    public String getAfter() {
        return after;
    }

    /**
     *
     * @param after
     * The after
     */
    public void setAfter(String after) {
        this.after = after;
    }

    /**
     *
     * @return
     * The before
     */
    public Object getBefore() {
        return before;
    }

    /**
     *
     * @param before
     * The before
     */
    public void setBefore(Object before) {
        this.before = before;
    }

}