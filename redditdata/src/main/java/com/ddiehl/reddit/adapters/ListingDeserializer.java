package com.ddiehl.reddit.adapters;

import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.RedditComment;
import com.ddiehl.reddit.listings.RedditLink;
import com.ddiehl.reddit.listings.RedditMoreComments;
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
        Listing listing;
        switch (kind) {
            case "t1":
                listing = context.deserialize(json, RedditComment.class);
                return listing;
            case "t3":
                listing = context.deserialize(json, RedditLink.class);
                return listing;
            case "more":
                listing = context.deserialize(json, RedditMoreComments.class);
                return listing;
            default:
                System.out.println("No deserialization class set for listing type: " + kind);
                return null;
        }
    }

}
