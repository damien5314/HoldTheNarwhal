package com.ddiehl.android.simpleredditreader.model.listings;

import com.google.gson.annotations.Expose;

import java.util.List;

public class ListingResponseData<T> {

    @Expose
    private String modhash;
    @Expose
    private List<T> children;
    @Expose
    private String after;
    @Expose
    private String before;


    public String getModhash() {
        return modhash;
    }

    public void setModhash(String modhash) {
        this.modhash = modhash;
    }

    public List<T> getChildren() {
        return children;
    }

    public void setChildren(List<T> children) {
        this.children = children;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
    }
}
