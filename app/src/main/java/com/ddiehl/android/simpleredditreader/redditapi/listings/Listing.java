package com.ddiehl.android.simpleredditreader.redditapi.listings;

import com.google.gson.annotations.Expose;

public abstract class Listing {

    @Expose
    private String kind;


    public String getKind() {
        return kind;
    }

    public abstract Object getData();
}
