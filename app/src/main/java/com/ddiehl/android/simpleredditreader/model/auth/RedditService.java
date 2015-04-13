package com.ddiehl.android.simpleredditreader.model.auth;

import android.content.Context;

import com.ddiehl.android.simpleredditreader.RedditReaderApplication;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.events.CommentsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.GetUserIdentityEvent;
import com.ddiehl.android.simpleredditreader.events.LinksLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.LoadCommentsEvent;
import com.ddiehl.android.simpleredditreader.events.LoadLinksEvent;
import com.ddiehl.android.simpleredditreader.events.LoadMoreCommentsEvent;
import com.ddiehl.android.simpleredditreader.events.MoreCommentsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.UserIdentityRetrievedEvent;
import com.ddiehl.android.simpleredditreader.events.VoteEvent;
import com.ddiehl.android.simpleredditreader.events.VoteSubmittedEvent;
import com.ddiehl.android.simpleredditreader.exceptions.UserRequiredException;
import com.ddiehl.android.simpleredditreader.model.adapters.ListingDeserializer;
import com.ddiehl.android.simpleredditreader.model.adapters.ListingResponseDeserializer;
import com.ddiehl.android.simpleredditreader.model.identity.UserIdentity;
import com.ddiehl.android.simpleredditreader.model.listings.Listing;
import com.ddiehl.android.simpleredditreader.model.listings.ListingResponse;
import com.ddiehl.android.simpleredditreader.model.listings.RedditMoreComments;
import com.ddiehl.android.simpleredditreader.model.web.RedditEndpoint;
import com.ddiehl.android.simpleredditreader.utils.BaseUtils;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.InputStream;
import java.util.List;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

public class RedditService implements IRedditService {
    private static final String TAG = RedditService.class.getSimpleName();

    private Context mContext;
    private Bus mBus;
    private RedditAPI mAPI;

    private String mAuthToken;

    protected RedditService(Context context) {
        mContext = context.getApplicationContext();
        mBus = BusProvider.getInstance();
        mBus.register(this);
        mAPI = buildApi();
    }

    private RedditAPI buildApi() {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(ListingResponse.class, new ListingResponseDeserializer())
                .registerTypeAdapter(Listing.class, new ListingDeserializer())
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(RedditEndpoint.AUTHORIZED)
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addHeader("User-Agent", RedditReaderApplication.USER_AGENT);
                        request.addHeader("Authorization", "bearer " + mAuthToken);
                    }
                })
                .build();

        return restAdapter.create(RedditAPI.class);
    }

    public void setAuthToken(String token) {
        mAuthToken = token;
    }

    @Subscribe
    public void onGetUserIdentity(GetUserIdentityEvent event) {
        mAPI.getUserIdentity(new Callback<UserIdentity>() {
            @Override
            public void success(UserIdentity userIdentity, Response response) {
                BaseUtils.printResponseStatus(response);
                mBus.post(new UserIdentityRetrievedEvent(userIdentity));
            }

            @Override
            public void failure(RetrofitError error) {
                BaseUtils.showError(mContext, error);
                BaseUtils.printResponse(error.getResponse());
                mBus.post(new UserIdentityRetrievedEvent(error));
            }
        });
    }

    public void onLoadLinks(LoadLinksEvent event) {
        String subreddit = event.getSubreddit();
        String sort = event.getSort();
        String timespan = event.getTimeSpan();
        String after = event.getAfter();

        if (subreddit == null) {
            mAPI.getDefaultLinks(sort, timespan, after, new Callback<ListingResponse>() {
                @Override
                public void success(ListingResponse linksResponse, Response response) {
                    BaseUtils.printResponseStatus(response);
                    mBus.post(new LinksLoadedEvent(linksResponse));
                }

                @Override
                public void failure(RetrofitError error) {
                    BaseUtils.showError(mContext, error);
                    BaseUtils.printResponse(error.getResponse());
                    mBus.post(new LinksLoadedEvent(error));
                }
            });
        } else {
            mAPI.getLinks(subreddit, sort, timespan, after, new Callback<ListingResponse>() {
                @Override
                public void success(ListingResponse linksResponse, Response response) {
                    BaseUtils.printResponseStatus(response);
                    mBus.post(new LinksLoadedEvent(linksResponse));
                }

                @Override
                public void failure(RetrofitError error) {
                    BaseUtils.showError(mContext, error);
                    BaseUtils.printResponse(error.getResponse());
                    mBus.post(new LinksLoadedEvent(error));
                }
            });
        }
    }

    public void onLoadComments(LoadCommentsEvent event) {
        String subreddit = event.getSubreddit();
        String article = event.getArticle();
        String sort = event.getSort();

        mAPI.getComments(subreddit, article, sort, new Callback<List<ListingResponse>>() {
            @Override
            public void success(List<ListingResponse> listingsList, Response response) {
                BaseUtils.printResponseStatus(response);
                mBus.post(new CommentsLoadedEvent(listingsList));
            }

            @Override
            public void failure(RetrofitError error) {
                BaseUtils.showError(mContext, error);
                BaseUtils.printResponse(error.getResponse());
                mBus.post(new CommentsLoadedEvent(error));
            }
        });
    }

//    link_id=t3_32c9vd&
//    sort=top&
//    children=cq9zth3%2Ccqa1vhh%2Ccqa8ak3&
//    depth=7&
//    id=t1_cq9zth3&
//    r=gaming&
//    uh=6lgjdu4p67bf2529e5faf328fb875fa269838157d5fbbaa828&
//    renderstyle=html

    public void onLoadMoreComments(LoadMoreCommentsEvent event) {
        RedditMoreComments moreComments = event.getMoreComments();
        String linkId = moreComments.getParentId();
        String sort = event.getSort(); // How can we get the sort?

        List<String> childList = moreComments.getChildren();
        String children = "";
        for (int i = 0; i < childList.size(); i++) {
            children += childList.get(i) + ",";
        }
        children = children.substring(0, children.length() - 1); // Delete the last comma

        String depth = String.valueOf(moreComments.getDepth());
        String id = moreComments.getId();
        String subreddit = null; // How can we get the subreddit?

//        mAPI.getMoreChildren(linkId, sort, children, depth, id, subreddit, new Callback<List<ListingResponse>>() {
        mAPI.getMoreChildren(linkId, sort, children, depth, id, new Callback<List<ListingResponse>>() {
//        mAPI.getMoreChildren(linkId, children, depth, id, new Callback<List<ListingResponse>>() {
            @Override
            public void success(List<ListingResponse> listingResponses, Response response) {
//                BaseUtils.printResponseStatus(response);
                BaseUtils.printResponse(response);
                mBus.post(new MoreCommentsLoadedEvent(listingResponses));
            }

            @Override
            public void failure(RetrofitError error) {
                BaseUtils.showError(mContext, error);
                BaseUtils.printResponse(error.getResponse());
                mBus.post(new MoreCommentsLoadedEvent(error));
            }
        });
    }

    public void onVote(VoteEvent event) {
        final String type = event.getType();
        final String id = event.getId();
        String fullname = String.format("%s_%s", type, id);
        final int direction = event.getDirection();
        mAPI.vote(fullname, direction, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                BaseUtils.printResponseStatus(response);

                try {
                    InputStream in = response.getBody().in();
                    if (BaseUtils.inputStreamToString(in).contains("USER_REQUIRED")) {
                        throw new UserRequiredException();
                    } else {
                        mBus.post(new VoteSubmittedEvent(id, direction));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                BaseUtils.showError(mContext, error);
                BaseUtils.printResponse(error.getResponse());
                mBus.post(new VoteSubmittedEvent(error));
            }
        });
    }

}
