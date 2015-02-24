package com.ddiehl.android.simpleredditreader.model.listings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@SuppressWarnings("unused")
public class RedditMoreComments extends Listing {

    @Expose
    private RedditMoreCommentsData data;


    @Override
    public RedditMoreCommentsData getData() {
        return null;
    }

    public int getDepth() {
        return data.depth;
    }

    public void setDepth(int depth) {
        data.depth = depth;
    }

    public boolean isVisible() {
        return data.isVisible;
    }

    public void isVisible(boolean b) {
        data.isVisible = b;
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

        private int depth;
        private boolean isVisible = true;

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
}
