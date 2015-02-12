package com.ddiehl.android.simpleredditreader.redditapi.listings;

import com.google.gson.annotations.Expose;

/**
 * Created by Damien on 2/5/2015.
 */
public class MediaEmbed {

    @Expose
    private String content;
    @Expose
    private Integer width;
    @Expose
    private Boolean scrolling;
    @Expose
    private Integer height;

    /**
     *
     * @return
     * The content
     */
    public String getContent() {
        return content;
    }

    /**
     *
     * @param content
     * The content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     *
     * @return
     * The width
     */
    public Integer getWidth() {
        return width;
    }

    /**
     *
     * @param width
     * The width
     */
    public void setWidth(Integer width) {
        this.width = width;
    }

    /**
     *
     * @return
     * The scrolling
     */
    public Boolean getScrolling() {
        return scrolling;
    }

    /**
     *
     * @param scrolling
     * The scrolling
     */
    public void setScrolling(Boolean scrolling) {
        this.scrolling = scrolling;
    }

    /**
     *
     * @return
     * The height
     */
    public Integer getHeight() {
        return height;
    }

    /**
     *
     * @param height
     * The height
     */
    public void setHeight(Integer height) {
        this.height = height;
    }

}