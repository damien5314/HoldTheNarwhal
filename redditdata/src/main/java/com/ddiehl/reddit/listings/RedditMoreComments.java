package com.ddiehl.reddit.listings;

import com.google.gson.annotations.Expose;

import java.util.List;

@SuppressWarnings("unused")
public class RedditMoreComments extends AbsRedditComment<RedditMoreComments.Data> {

    public Integer getCount() {
        return data.count;
    }

    public List<String> getChildren() {
        return data.children;
    }

    public static class Data extends AbsRedditComment.Data {
        @Expose private Integer count;
        @Expose private List<String> children;
    }

    @Override
    public String toString() {
        return "MoreComments (" + getCount() + ")";
    }
}
