package com.ddiehl.reddit.listings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@SuppressWarnings("unused")
public class RedditMoreComments extends AbsRedditComment {

    @Expose
    private RedditMoreCommentsData data;


    @Override
    public RedditMoreCommentsData getData() {
        return data;
    }

    public Integer getCount() {
        return data.count;
    }

    public String getParentId() {
        return data.parentId;
    }

    public String getId() {
        return data.id;
    }

    public String getName() {
        return data.name;
    }

    public List<String> getChildren() {
        return data.children;
    }

    private static class RedditMoreCommentsData {

        @Expose
        private Integer count;
        @Expose @SerializedName("parent_id")
        private String parentId;
        @Expose
        private String id;
        @Expose
        private String name;
        @Expose
        private List<String> children;
        
    }

    @Override
    public String toString() {
        return "MoreComments (" + getCount() + ")";
    }
}
