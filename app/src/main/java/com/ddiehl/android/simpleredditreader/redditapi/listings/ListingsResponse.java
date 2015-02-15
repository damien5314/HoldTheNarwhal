package com.ddiehl.android.simpleredditreader.redditapi.listings;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Damien on 2/5/2015.
 */
public class ListingsResponse {

    @Expose
    private String kind;
    @Expose
    private ListingsResponseData data;

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
    public ListingsResponseData getData() {
        return data;
    }

    /**
     *
     * @param data
     * The data
     */
    public void setData(ListingsResponseData data) {
        this.data = data;
    }

    public static class ListingsResponseData {

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
}
