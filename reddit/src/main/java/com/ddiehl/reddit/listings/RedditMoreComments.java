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

    @Override
    public boolean isCollapsed() {
        return false;
    }

    @Override
    public void setCollapsed(boolean b) {
        throw new UnsupportedOperationException("Cannot collapse a comment stub");
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
