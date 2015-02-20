package com.ddiehl.android.simpleredditreader.model.adapters;

import com.ddiehl.android.simpleredditreader.model.listings.ListingResponse;
import com.ddiehl.android.simpleredditreader.model.listings.RedditComment;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class CommentsListingAdapter extends TypeAdapter<ListingResponse<RedditComment>> {

    @Override
    public ListingResponse<RedditComment> read(JsonReader in) throws IOException {

        return null;
    }

    @Override
    public void write(JsonWriter out, ListingResponse value) throws IOException {
        // Not needed, we do not currently serialize JSON objects
    }

}
