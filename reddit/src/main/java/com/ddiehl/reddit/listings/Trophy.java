/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.reddit.listings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Trophy extends Listing<Trophy.Data> {

    public String getIcon70() {
        return data.icon70;
    }

    public String getIcon40() {
        return data.icon40;
    }

    public String getDescription() {
        return data.description;
    }

    public String getUrl() {
        return data.url;
    }

    public String getName() {
        return data.name;
    }

    public String getAwardId() {
        return data.awardId;
    }

    static class Data extends Listing.Data {
        @Expose @SerializedName("icon_70") String icon70;
        @Expose @SerializedName("icon_40") String icon40;
        @Expose String description;
        @Expose String url;
        @Expose String name;
        @Expose @SerializedName("award_id") String awardId;
    }
}
