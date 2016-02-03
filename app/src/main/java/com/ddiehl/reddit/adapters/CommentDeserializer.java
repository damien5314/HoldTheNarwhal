package com.ddiehl.reddit.adapters;

import com.ddiehl.android.dlogger.Logger;
import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.CommentStub;
import com.ddiehl.reddit.listings.Listing;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class CommentDeserializer implements JsonDeserializer<Listing> {
  private Logger mLog = HoldTheNarwhal.getLogger();

  @Override
  public Listing deserialize(
      JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    JsonObject o = json.getAsJsonObject();
    String kind = o.get("kind").getAsString();
    Listing listing;
    switch (kind) {
      case "t1":
        listing = context.deserialize(json, Comment.class);
        return listing;
      case "more":
        listing = context.deserialize(json, CommentStub.class);
        return listing;
      default:
        mLog.e("No deserialization class set for listing type: " + kind);
        return null;
    }
  }
}