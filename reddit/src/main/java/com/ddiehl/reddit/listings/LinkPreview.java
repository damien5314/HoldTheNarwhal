/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.reddit.listings;


import com.google.gson.annotations.Expose;

import java.util.List;

public class LinkPreview {
    @Expose
    List<Image> images;

    public static class Image {
        @Expose
        Res source;
        @Expose List<Res> resolutions;
        @Expose Variants variants;
        String id;

        public Res getSource() {
            return source;
        }

        public List<Res> getResolutions() {
            return resolutions;
        }

        public Variants getVariants() {
            return variants;
        }

        public String getId() {
            return id;
        }

        public static class Variants {
            @Expose public Image nsfw;
            // TODO What other variants are available to expose?
        }

        public static class Res {
            @Expose String url;
            @Expose int width;
            @Expose int height;

            public String getUrl() {
                return url;
            }

            public int getWidth() {
                return width;
            }

            public int getHeight() {
                return height;
            }
        }
    }
}
