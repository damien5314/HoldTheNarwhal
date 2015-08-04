/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.reddit.listings;


import com.google.gson.annotations.Expose;

import java.util.List;

public class TrophyResponseData {

    @Expose
    private List<Listing> trophies;

    public List<Listing> getTrophies() {
        return trophies;
    }

}