package com.ddiehl.reddit.adapters;

import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.CommentStub;
import com.ddiehl.reddit.listings.Link;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.PrivateMessage;
import com.ddiehl.reddit.listings.Subreddit;
import com.ddiehl.reddit.listings.Trophy;
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
        listing = context.deserialize(json, Comment.class);
        Comment comment = (Comment) listing;
        if (comment.isScoreHidden()) comment.setScore(null);
        return listing;
      case "t3":
        listing = context.deserialize(json, Link.class);
        Link link = (Link) listing;
        if (link.isScoreHidden()) link.setScore(null);
        return listing;
      case "t4":
        listing = context.deserialize(json, PrivateMessage.class);
        return listing;
      case "t5":
        listing = context.deserialize(json, Subreddit.class);
        return listing;
      case "t6":
        listing = context.deserialize(json, Trophy.class);
        return listing;
      case "more":
        listing = context.deserialize(json, CommentStub.class);
        return listing;
      default:
        return null;
    }
  }

}
