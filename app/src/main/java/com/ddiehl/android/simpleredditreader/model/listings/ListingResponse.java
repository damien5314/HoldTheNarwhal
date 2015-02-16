package com.ddiehl.android.simpleredditreader.model.listings;

import com.google.gson.annotations.Expose;

import java.util.List;

public class ListingResponse<T extends Listing> {

    @Expose
    private String kind;
    @Expose
    private ListingResponseData<T> data;


    public String getKind() {
        return kind;
    }

    public ListingResponseData<T> getData() {
        return data;
    }

    public static class ListingResponseData<T> {

        @Expose
        private String modhash;
        @Expose
        private List<T> children;
        @Expose
        private String after;
        @Expose
        private String before;


        public String getModhash() {
            return modhash;
        }

        public List<T> getChildren() {
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
