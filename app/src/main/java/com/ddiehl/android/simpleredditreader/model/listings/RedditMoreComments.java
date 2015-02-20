package com.ddiehl.android.simpleredditreader.model.listings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RedditMoreComments extends Comment {

    @Expose
    private RedditMoreCommentsData data;


    @Override
    public Object getData() {
        return null;
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
}
