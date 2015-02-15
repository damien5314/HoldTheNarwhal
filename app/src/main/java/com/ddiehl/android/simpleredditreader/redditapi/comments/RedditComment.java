package com.ddiehl.android.simpleredditreader.redditapi.comments;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class RedditComment {

    @Expose
    private String kind;
    @Expose
    private RedditCommentData data;

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
    public RedditCommentData getData() {
        return data;
    }

    /**
     *
     * @param data
     * The data
     */
    public void setData(RedditCommentData data) {
        this.data = data;
    }

    public static class RedditCommentData {

        @Expose
        private Integer count;
        @SerializedName("parent_id")
        @Expose
        private String parentId;
        @Expose
        private String id;
        @Expose
        private String name;
        @Expose
        private List<String> children = new ArrayList<>();

        /**
         *
         * @return
         * The count
         */
        public Integer getCount() {
            return count;
        }

        /**
         *
         * @param count
         * The count
         */
        public void setCount(Integer count) {
            this.count = count;
        }

        /**
         *
         * @return
         * The parentId
         */
        public String getParentId() {
            return parentId;
        }

        /**
         *
         * @param parentId
         * The parent_id
         */
        public void setParentId(String parentId) {
            this.parentId = parentId;
        }

        /**
         *
         * @return
         * The id
         */
        public String getId() {
            return id;
        }

        /**
         *
         * @param id
         * The id
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         *
         * @return
         * The name
         */
        public String getName() {
            return name;
        }

        /**
         *
         * @param name
         * The name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         *
         * @return
         * The children
         */
        public List<String> getChildren() {
            return children;
        }

        /**
         *
         * @param children
         * The children
         */
        public void setChildren(List<String> children) {
            this.children = children;
        }

    }
}
