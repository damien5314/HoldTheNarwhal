package com.ddiehl.android.simpleredditreader.web;

import android.content.Context;
import android.util.Log;

import com.ddiehl.android.simpleredditreader.RedditAuthorization;
import com.ddiehl.android.simpleredditreader.RedditPreferences;
import com.ddiehl.android.simpleredditreader.Utils;
import com.ddiehl.android.simpleredditreader.events.ApplicationAuthorizedEvent;
import com.ddiehl.android.simpleredditreader.events.AuthorizationEvent;
import com.ddiehl.android.simpleredditreader.events.AuthorizeApplicationEvent;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.events.CommentsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.LinksLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.LoadCommentsEvent;
import com.ddiehl.android.simpleredditreader.events.LoadLinksEvent;
import com.ddiehl.android.simpleredditreader.events.VoteEvent;
import com.ddiehl.android.simpleredditreader.events.VoteSubmittedEvent;
import com.ddiehl.android.simpleredditreader.model.adapters.ListingDeserializer;
import com.ddiehl.android.simpleredditreader.model.adapters.ListingResponseDeserializer;
import com.ddiehl.android.simpleredditreader.model.listings.Listing;
import com.ddiehl.android.simpleredditreader.model.listings.ListingResponse;
import com.ddiehl.android.simpleredditreader.model.listings.RedditComment;
import com.ddiehl.android.simpleredditreader.model.listings.RedditMoreComments;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import retrofit.Callback;
import retrofit.Endpoint;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;


public class RedditService {
    private static final String TAG = RedditService.class.getSimpleName();

    private static final String USER_AGENT =
            "android:com.ddiehl.android.simpleredditreader:v0.1 (by /u/damien5314)";

    private static RedditService _instance = new RedditService();

    private RedditApi mApi;
    private RedditEndpoint mEndpoint;
    private Bus mBus = BusProvider.getInstance();

    private RedditService() {
        mEndpoint = new RedditEndpoint();
        mEndpoint.setUrl(RedditEndpoint.NORMAL);
        mApi = buildApi();
    }

    public static RedditService getInstance() {
        return _instance;
    }

    private RedditApi buildApi() {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(ListingResponse.class, new ListingResponseDeserializer())
                .registerTypeAdapter(Listing.class, new ListingDeserializer())
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(mEndpoint)
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addHeader("User-Agent", USER_AGENT);
                        request.addHeader("Authorization", RedditAuthorization.getAuthHeader());
                    }
                })
                .build();

        return restAdapter.create(RedditApi.class);
    }

    /**
     * Notification that authentication state has changed
     */
    @Subscribe
    public void onAuthorizationEvent(AuthorizationEvent event) {

    }

    @Subscribe
    public void onAuthorizeApplication(AuthorizeApplicationEvent event) {
        Context context = event.getContext();
        String grantType = "https://oauth.reddit.com/grants/installed_client";
        String deviceId = RedditPreferences.getDeviceId(context);

        Log.i(TAG, "Attempting to authorize application...");
        mApi.getApplicationAccessToken(grantType, deviceId, new Callback<AccessTokenResponse>() {
            @Override
            public void success(AccessTokenResponse accessTokenResponse, Response response) {
                mEndpoint.setUrl(RedditEndpoint.AUTHORIZED);
                mBus.post(new ApplicationAuthorizedEvent(accessTokenResponse));
            }

            @Override
            public void failure(RetrofitError error) {
                mBus.post(new ApplicationAuthorizedEvent(error));
            }
        });
    }

    /**
     * Retrieves link listings for subreddit
     */
    @Subscribe
    public void onLoadLinks(LoadLinksEvent event) {
        String subreddit = event.getSubreddit();
        String sort = event.getSort();
        String timespan = event.getTimeSpan();
        String after = event.getAfter();
        Log.i(TAG, "Loading links for /r/" + subreddit + "/" + sort + ".json?t=" + timespan);

        if (subreddit == null) {
            mApi.getDefaultLinks(sort, timespan, after, new Callback<ListingResponse>() {
                @Override
                public void success(ListingResponse linksResponse, Response response) {
                    Utils.printResponse(response);
                    mBus.post(new LinksLoadedEvent(linksResponse));
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e(TAG, "RetrofitError: " + error.getMessage(), error);
                }
            });
        } else {
            mApi.getLinks(subreddit, sort, timespan, after, new Callback<ListingResponse>() {
                @Override
                public void success(ListingResponse linksResponse, Response response) {
                    mBus.post(new LinksLoadedEvent(linksResponse));
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e(TAG, "RetrofitError: " + error.getMessage(), error);
                    mBus.post(new LinksLoadedEvent(error));
                }
            });
        }
    }

    /**
     * Retrieves comment listings for link passed as parameter
     */
    @Subscribe
    public void onLoadComments(LoadCommentsEvent event) {
        String subreddit = event.getSubreddit();
        String article = event.getArticle();
        String sort = event.getSort();

        Log.i(TAG, "Loading /r/" + subreddit + "/comments/" + article + "/.json?sort=" + sort);
        mApi.getComments(subreddit, article, sort, new Callback<List<ListingResponse>>() {
            @Override
            public void success(List<ListingResponse> listingsList, Response response) {
                Log.i(TAG, "Comments loaded successfully");
                flattenList(listingsList.get(1));
                mBus.post(new CommentsLoadedEvent(listingsList));
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "RetrofitError: " + error.getMessage(), error);
                mBus.post(new CommentsLoadedEvent(error));
            }
        });
    }

    /**
     * Flattens list of comments, marking each comment with depth
     */
    private void flattenList(ListingResponse commentsResponse) {
        List<Listing> commentList = commentsResponse.getData().getChildren();
        int i = 0;
        while (i < commentList.size()) {
            Listing listing = commentList.get(i);
            if (listing instanceof RedditComment) {
                RedditComment comment = (RedditComment) listing;
                ListingResponse replies = comment.getReplies();
                if (replies != null) {
                    flattenList(replies);
                }
                comment.setDepth(comment.getDepth() + 1); // Increase depth by 1
                if (comment.getReplies() != null) {
                    commentList.addAll(i+1, comment.getReplies().getData().getChildren()); // Add all of the replies to commentList
                    comment.setReplies(null); // Remove replies for comment
                }
            } else { // Listing is a RedditMoreComments
                RedditMoreComments moreComments = (RedditMoreComments) listing;
                moreComments.setDepth(moreComments.getDepth() + 1); // Increase depth by 1
            }
            i++;
        }

    }

    /**
     * Submits a vote on a link or comment
     */
    @Subscribe
    public void onVote(VoteEvent event) {
        String id = event.getId();
        int direction = event.getDirection();
        mApi.vote(id, direction, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                mBus.post(new VoteSubmittedEvent(response));
            }

            @Override
            public void failure(RetrofitError error) {
                mBus.post(new VoteSubmittedEvent(error));
            }
        });
    }

    public static class RedditEndpoint implements Endpoint {
        public static final String NORMAL = "https://www.reddit.com";
        public static final String AUTHORIZED = "https://oauth.reddit.com";

        private String mUrl;

        public void setUrl(String url) {
            mUrl = url;
        }

        @Override
        public String getUrl() {
            return mUrl;
        }

        @Override
        public String getName() {
            return "default";
        }
    }
}
