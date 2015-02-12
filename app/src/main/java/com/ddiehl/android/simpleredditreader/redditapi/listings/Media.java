package com.ddiehl.android.simpleredditreader.redditapi.listings;

import com.google.gson.annotations.Expose;

/**
 * Created by Damien on 2/5/2015.
 */
public class Media {

    @Expose
    private Oembed oembed;
    @Expose
    private String type;

    /**
     *
     * @return
     * The oembed
     */
    public Oembed getOembed() {
        return oembed;
    }

    /**
     *
     * @param oembed
     * The oembed
     */
    public void setOembed(Oembed oembed) {
        this.oembed = oembed;
    }

    /**
     *
     * @return
     * The type
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @param type
     * The type
     */
    public void setType(String type) {
        this.type = type;
    }

}