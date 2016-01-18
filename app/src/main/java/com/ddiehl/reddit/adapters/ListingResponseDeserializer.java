package com.ddiehl.reddit.adapters;

import com.ddiehl.reddit.listings.ListingResponse;
import com.ddiehl.reddit.listings.ListingResponseData;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Handles the special case for the replies node in a Comment object
 * where there are no replies and the json is ""
 */
public class ListingResponseDeserializer implements JsonDeserializer<ListingResponse> {
  @Override
  public ListingResponse deserialize(
      JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    if (json.isJsonPrimitive()) return null;
    JsonObject obj = json.getAsJsonObject();
    String kind = obj.get("kind").getAsString();
    ListingResponseData data = context.deserialize(
        obj.get("data"), ListingResponseData.class);
    ListingResponse response = new ListingResponse();
    response.setKind(kind);
    response.setData(data);
    return response;
  }
}
