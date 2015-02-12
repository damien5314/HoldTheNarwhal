package com.ddiehl.android.simpleredditreader.redditapi.listings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Damien on 2/5/2015.
 */
public class Oembed {

    @SerializedName("provider_url")
    @Expose
    private String providerUrl;
    @Expose
    private String description;
    @Expose
    private String title;
    @Expose
    private String type;
    @SerializedName("thumbnail_width")
    @Expose
    private Integer thumbnailWidth;
    @Expose
    private Integer height;
    @Expose
    private Integer width;
    @Expose
    private String html;
    @Expose
    private String version;
    @SerializedName("provider_name")
    @Expose
    private String providerName;
    @SerializedName("thumbnail_url")
    @Expose
    private String thumbnailUrl;
    @SerializedName("thumbnail_height")
    @Expose
    private Integer thumbnailHeight;

    /**
     *
     * @return
     * The providerUrl
     */
    public String getProviderUrl() {
        return providerUrl;
    }

    /**
     *
     * @param providerUrl
     * The provider_url
     */
    public void setProviderUrl(String providerUrl) {
        this.providerUrl = providerUrl;
    }

    /**
     *
     * @return
     * The description
     */
    public String getDescription() {
        return description;
    }

    /**
     *
     * @param description
     * The description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     * @return
     * The title
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @param title
     * The title
     */
    public void setTitle(String title) {
        this.title = title;
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

    /**
     *
     * @return
     * The thumbnailWidth
     */
    public Integer getThumbnailWidth() {
        return thumbnailWidth;
    }

    /**
     *
     * @param thumbnailWidth
     * The thumbnail_width
     */
    public void setThumbnailWidth(Integer thumbnailWidth) {
        this.thumbnailWidth = thumbnailWidth;
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
     * The html
     */
    public String getHtml() {
        return html;
    }

    /**
     *
     * @param html
     * The html
     */
    public void setHtml(String html) {
        this.html = html;
    }

    /**
     *
     * @return
     * The version
     */
    public String getVersion() {
        return version;
    }

    /**
     *
     * @param version
     * The version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     *
     * @return
     * The providerName
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     *
     * @param providerName
     * The provider_name
     */
    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    /**
     *
     * @return
     * The thumbnailUrl
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    /**
     *
     * @param thumbnailUrl
     * The thumbnail_url
     */
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    /**
     *
     * @return
     * The thumbnailHeight
     */
    public Integer getThumbnailHeight() {
        return thumbnailHeight;
    }

    /**
     *
     * @param thumbnailHeight
     * The thumbnail_height
     */
    public void setThumbnailHeight(Integer thumbnailHeight) {
        this.thumbnailHeight = thumbnailHeight;
    }

}
