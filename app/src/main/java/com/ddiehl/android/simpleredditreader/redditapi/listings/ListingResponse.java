package com.ddiehl.android.simpleredditreader.redditapi.listings;

import com.google.gson.annotations.Expose;

import java.util.List;

public class ListingResponse {

    @Expose
    private String kind;
    @Expose
    private ListingResponseData data;


    public String getKind() {
        return kind;
    }

    public ListingResponseData getData() {
        return data;
    }

    public static class ListingResponseData {

        @Expose
        private String modhash;
        @Expose
        private List<Listing> children;
        @Expose
        private String after;
        @Expose
        private String before;


        public String getModhash() {
            return modhash;
        }

        public List<Listing> getChildren() {
            return children;
        }

        public String getAfter() {
            return after;
        }

        public String getBefore() {
            return before;
        }
    }
}
