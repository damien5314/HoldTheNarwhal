package com.ddiehl.android.simpleredditreader.web;

import android.content.Context;
import android.util.Log;

import com.ddiehl.android.simpleredditreader.RedditAuthorization;
import com.ddiehl.android.simpleredditreader.RedditPreferences;
import com.ddiehl.android.simpleredditreader.Utils;
import com.ddiehl.android.simpleredditreader.events.ApplicationAuthorizedEvent;
import com.ddiehl.android.simpleredditreader.events.AuthorizeUserEvent;
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

    private static RedditService _instance;

    private Context mContext;
    private RedditApi mApi;
    private RedditEndpoint mEndpoint;
    private Bus mBus = BusProvider.getInstance();
    private RedditAuthorization mRedditAuthorization;

    private RedditService(Context context) {
        mContext = context.getApplicationContext();
        mEndpoint = new RedditEndpoint();
        mEndpoint.setUrl(RedditEndpoint.NORMAL);
        mApi = buildApi();
        mRedditAuthorization = RedditAuthorization.getInstance(mContext);
    }

    public static RedditService getInstance(Context context) {
        if (_instance == null) {
            synchronized (RedditService.class) {
                if (_instance == null) {
                    _instance = new RedditService(context);
                }
            }
        }
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
                        request.addHeader("Authorization", mRedditAuthorization.getAuthHeader());
                    }
                })
                .build();

        return restAdapter.create(RedditApi.class);
    }

    /**
     * Notification that authentication state has changed
     */
    @Subscribe
    public void onAuthorizeUser(AuthorizeUserEvent event) {

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
                Utils.printResponseStatus(response);
                mEndpoint.setUrl(RedditEndpoint.AUTHORIZED);
                mBus.post(new ApplicationAuthorizedEvent(accessTokenResponse));
            }

            @Override
            public void failure(RetrofitError error) {
                Utils.printResponse(error.getResponse());
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

        if (subreddit == null) {
            mApi.getDefaultLinks(sort, timespan, after, new Callback<ListingResponse>() {
                @Override
                public void success(ListingResponse linksResponse, Response response) {
                    Utils.printResponseStatus(response);
                    mBus.post(new LinksLoadedEvent(linksResponse));
                }

                @Override
                public void failure(RetrofitError error) {
                    Utils.printResponse(error.getResponse());
                }
            });
        } else {
            mApi.getLinks(subreddit, sort, timespan, after, new Callback<ListingResponse>() {
                @Override
                public void success(ListingResponse linksResponse, Response response) {
                    Utils.printResponseStatus(response);
                    mBus.post(new LinksLoadedEvent(linksResponse));
                }

                @Override
                public void failure(RetrofitError error) {
                    Utils.printResponse(error.getResponse());
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

        mApi.getComments(subreddit, article, sort, new Callback<List<ListingResponse>>() {
            @Override
            public void success(List<ListingResponse> listingsList, Response response) {
                Utils.printResponseStatus(response);
                mBus.post(new CommentsLoadedEvent(listingsList));
            }

            @Override
            public void failure(RetrofitError error) {
                Utils.printResponse(error.getResponse());
                mBus.post(new CommentsLoadedEvent(error));
            }
        });
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
                Utils.printResponseStatus(response);
                Utils.printResponseBody(response);
                mBus.post(new VoteSubmittedEvent(response));
            }

            @Override
            public void failure(RetrofitError error) {
                Utils.printResponse(error.getResponse());
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
