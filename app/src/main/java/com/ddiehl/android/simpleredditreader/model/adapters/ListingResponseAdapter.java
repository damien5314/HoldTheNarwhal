package com.ddiehl.android.simpleredditreader.model.adapters;

import com.ddiehl.android.simpleredditreader.model.listings.ListingResponse;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class ListingResponseAdapter extends TypeAdapter<ListingResponse> {

    @Override
    public ListingResponse read(JsonReader in) throws IOException {
        JsonToken token = in.peek();
        if (token.name().equals("kind") && in.nextString().equals("more")) {

        }
        return null;
    }

    @Override
    public void write(JsonWriter out, ListingResponse value) throws IOException {

    }

}
