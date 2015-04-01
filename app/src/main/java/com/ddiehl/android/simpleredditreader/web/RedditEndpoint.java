package com.ddiehl.android.simpleredditreader.web;

import retrofit.Endpoint;

/**
 * Created by ddiehl on 3/31/15.
 */
public class RedditEndpoint implements Endpoint {
    public static final String NORMAL = "https://www.reddit.com";
    public static final String AUTHORIZED = "https://oauth.reddit.com";

    private String mUrl;

    public void setUrl(String url) {
        mUrl = url;
    }

    @Override
    public String getUrl() {
        return mUrl;
    }

    @Override
    public String getName() {
        return "default";
    }
}
