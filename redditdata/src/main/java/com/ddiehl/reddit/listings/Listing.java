package com.ddiehl.reddit.listings;

import com.google.gson.annotations.Expose;

public abstract class Listing {

    @Expose
    private String kind;


    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public abstract Object getData();
}
