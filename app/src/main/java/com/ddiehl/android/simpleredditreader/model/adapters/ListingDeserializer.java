package com.ddiehl.android.simpleredditreader.model.adapters;

import com.ddiehl.android.simpleredditreader.model.listings.Listing;
import com.ddiehl.android.simpleredditreader.model.listings.RedditComment;
import com.ddiehl.android.simpleredditreader.model.listings.RedditLink;
import com.ddiehl.android.simpleredditreader.model.listings.RedditMoreComments;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class ListingDeserializer implements JsonDeserializer<Listing> {

    @Override
    public Listing deserialize(
            JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject o = json.getAsJsonObject();
        String kind = o.get("kind").getAsString();
        switch (kind) {
            case "t1":
                return context.deserialize(json, RedditComment.class);
            case "t3":
                return context.deserialize(json, RedditLink.class);
            case "more":
                return context.deserialize(json, RedditMoreComments.class);
            default:
                return context.deserialize(json, typeOfT);
        }
    }
}
