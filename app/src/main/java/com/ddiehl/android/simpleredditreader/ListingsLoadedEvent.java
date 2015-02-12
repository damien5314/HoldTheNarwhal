package com.ddiehl.android.simpleredditreader;

import com.ddiehl.android.simpleredditreader.redditapi.listings.RedditListing;
import com.ddiehl.android.simpleredditreader.redditapi.listings.RedditListingData;
import com.ddiehl.android.simpleredditreader.redditapi.listings.RedditResponse;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import retrofit.client.Response;

/**
 * Created by Damien on 1/19/2015.
 */
public class ListingsLoadedEvent {
    Response mResponse;
    List<RedditListingData> mListings;

    public ListingsLoadedEvent(Response response) {
        mResponse = response;
        parseListings(mResponse);
    }

    public ListingsLoadedEvent(RedditResponse response) {
        mListings = new ArrayList<>();

        List<RedditListing> listings = response.getData().getChildren();

        for (RedditListing listing : listings) {
            mListings.add(listing.getData());
        }
    }

    // TODO
    public void parseListings(Response response) {
        mListings = new ArrayList<>();

//        mListings.add(new Link("It's my Reddit birthday!", null));
//        mListings.add(new Link("Check out my cat", null));
//        mListings.add(new Link("The narwhal bacons at midnight", null));

        // Build a JSON string from Response object body
//        StringBuilder json = new StringBuilder();
//        BufferedReader reader = null;
//        try {
//            InputStream in = response.getBody().in();
//            reader = new BufferedReader(new InputStreamReader(in));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                Utils.debug(line);
//                json.append(line);
//            }
//        } catch (IOException e) {
//            Utils.error("IOException: ", e);
//        } finally {
//            try {
//                if (reader != null)
//                    reader.close();
//            } catch (IOException e) {
//                Utils.error("IOException: ", e);
//            }
//        }

//        String jsonString = json.toString();
//        Utils.debug("JSON: " + jsonString);

        try {
            // Parse JSON string to JSON objects with Gson
            Gson gson = new Gson();
            gson.fromJson(new InputStreamReader(response.getBody().in()), Link.class);
        } catch (IOException e) {

        }
    }

    public List<RedditListingData> getListings() {
        return mListings;
    }
}
