package com.ddiehl.android.simpleredditreader.redditapi;

import android.os.AsyncTask;
import android.util.Log;

import com.ddiehl.android.simpleredditreader.events.ListingsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.LoadHotListingsEvent;
import com.ddiehl.android.simpleredditreader.redditapi.listings.ListingResponse;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedInput;

/**
 * Created by Damien on 1/19/2015.
 */
public class RedditService {
    private static final String TAG = RedditService.class.getSimpleName();

    private RedditApi mApi;
    private Bus mBus;

    public RedditService(RedditApi api, Bus bus) {
        mApi = api;
        mBus = bus;
    }

    @Subscribe
    public void onLoadHotListings(LoadHotListingsEvent event) {
        new GetHotListings().execute(event.getSubreddit());
    }

    /**
     * Retrieves /hot.json listings for subreddit passed as a parameter
     */
    private class GetHotListings extends AsyncTask<String, Void, ListingResponse> {
        @Override
        protected ListingResponse doInBackground(String... params) {
            Log.d(TAG, "Loading listings for " + params[0]);
            try {
                return mApi.getHotListing(params[0]);
            } catch (RetrofitError error) {
                Log.e(TAG, "RetrofitError: " + error.getMessage());
                return null;
            } catch (Exception error) {
                Log.e(TAG, "Exception: ", error);
                return null;
            }
        }

        @Override
        protected void onPostExecute(ListingResponse response) {
            if (response != null) {
//                printResponse(response);
                mBus.post(new ListingsLoadedEvent(response));
            } else {
                Log.e(TAG, "Response is null");
            }
        }
    }

    static void printResponse(Response response) {
        try {
/*
            System.out.println("STATUS: " + response.getStatus());
            System.out.println("URL:    " + response.getUrl());
            System.out.println("REASON: " + response.getReason());

            System.out.println("--HEADERS--");
            List<Header> headersList = response.getHeaders();
            for (Header header : headersList) {
                System.out.println(header.toString());
            }
*/

            System.out.println("--BODY--");
            TypedInput body = response.getBody();
            System.out.println("LENGTH: " + body.length());
            System.out.println("-CONTENT-");
            printInputStream(body.in());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void printInputStream(InputStream i) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(i));
        String inputLine;
        while ((inputLine = in.readLine()) != null)
            System.out.println(inputLine + "\n");
        in.close();
    }
}
