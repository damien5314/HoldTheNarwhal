/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.reddit.listings;

import com.google.gson.annotations.Expose;

public class ListingResponse {

    @Expose
    private String kind;
    @Expose
    private ListingResponseData data;


    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public ListingResponseData getData() {
        return data;
    }

    public void setData(ListingResponseData data) {
        this.data = data;
    }
}
