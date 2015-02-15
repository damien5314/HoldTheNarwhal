package com.ddiehl.android.simpleredditreader.redditapi.comments;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;


public class CommentsResponse {

    @Expose
    private String kind;
    @Expose
    private CommentsResponseData data;

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
    public CommentsResponseData getData() {
        return data;
    }

    /**
     *
     * @param data
     * The data
     */
    public void setData(CommentsResponseData data) {
        this.data = data;
    }

    public static class CommentsResponseData {

        @Expose
        private String modhash;
        @Expose
        private List<RedditComment> children = new ArrayList<>();
        @Expose
        private Object after;
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
        public List<RedditComment> getComments() {
            return children;
        }

        /**
         *
         * @param children
         * The children
         */
        public void setComments(List<RedditComment> children) {
            this.children = children;
        }

        /**
         *
         * @return
         * The after
         */
        public Object getAfter() {
            return after;
        }

        /**
         *
         * @param after
         * The after
         */
        public void setAfter(Object after) {
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

