/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.reddit.identity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Friend {

    @Expose @SerializedName("note")
    String mNote;

    public Friend(String note) {
        mNote = note;
    }
}