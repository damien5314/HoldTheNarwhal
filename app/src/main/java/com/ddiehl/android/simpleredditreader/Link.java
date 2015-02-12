package com.ddiehl.android.simpleredditreader;

import java.net.URL;

/**
 * Created by Damien on 1/19/2015.
 */
public class Link {
    private String title;
    private URL url;

    public Link(String title, URL url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public URL getURL() {
        return url;
    }

    public void setURL(URL URL) {
        url = URL;
    }
}
