package com.ddiehl.android.simpleredditreader.model.auth;

import android.content.Context;
import android.util.Log;

import com.ddiehl.android.simpleredditreader.RedditReaderApplication;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.events.CommentThreadLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.CommentsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.GetUserIdentityEvent;
import com.ddiehl.android.simpleredditreader.events.LinksLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.LoadCommentThreadEvent;
import com.ddiehl.android.simpleredditreader.events.LoadCommentsEvent;
import com.ddiehl.android.simpleredditreader.events.LoadLinksEvent;
import com.ddiehl.android.simpleredditreader.events.LoadMoreChildrenEvent;
import com.ddiehl.android.simpleredditreader.events.MoreChildrenLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.UserIdentityRetrievedEvent;
import com.ddiehl.android.simpleredditreader.events.VoteEvent;
import com.ddiehl.android.simpleredditreader.events.VoteSubmittedEvent;
import com.ddiehl.android.simpleredditreader.exceptions.UserRequiredException;
import com.ddiehl.android.simpleredditreader.model.adapters.AbsRedditCommentDeserializer;
import com.ddiehl.android.simpleredditreader.model.adapters.ListingDeserializer;
import com.ddiehl.android.simpleredditreader.model.adapters.ListingResponseDeserializer;
import com.ddiehl.android.simpleredditreader.model.identity.UserIdentity;
import com.ddiehl.android.simpleredditreader.model.listings.AbsRedditComment;
import com.ddiehl.android.simpleredditreader.model.listings.Listing;
import com.ddiehl.android.simpleredditreader.model.listings.ListingResponse;
import com.ddiehl.android.simpleredditreader.model.listings.MoreChildrenResponse;
import com.ddiehl.android.simpleredditreader.model.listings.RedditLink;
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
                .registerTypeAdapter(AbsRedditComment.class, new AbsRedditCommentDeserializer())
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

    @Override
    public void onLoadMoreChildren(LoadMoreChildrenEvent event) {
        RedditLink link = event.getRedditLink();
        final RedditMoreComments parentStub = event.getParentCommentStub();
        List<String> children = event.getChildren();
        String sort = event.getSort();

        StringBuilder b = new StringBuilder();
        for (String child : children)
            b.append(child).append(",");
        String childrenString = b.toString();
        childrenString = childrenString.substring(0, childrenString.length() - 1);

        mAPI.getMoreChildren(link.getName(), childrenString, sort, new Callback<MoreChildrenResponse>() {
            @Override
            public void success(MoreChildrenResponse moreChildrenResponse, Response response) {
                BaseUtils.printResponseStatus(response);
                mBus.post(new MoreChildrenLoadedEvent(parentStub, moreChildrenResponse));
            }

            @Override
            public void failure(RetrofitError error) {
                BaseUtils.showError(mContext, error);
                BaseUtils.printResponse(error.getResponse());
                mBus.post(new MoreChildrenLoadedEvent(error));
            }
        });
    }

    public void onLoadCommentThread(LoadCommentThreadEvent event) {
        RedditLink link = event.getLink();
        RedditMoreComments more = event.getMoreComments();

        String subreddit = link.getSubreddit();
        String article = link.getId();
        String commentId = more.getId();
        String sort = event.getSort();
        int context = 0;

        final int parentDepth = more.getDepth();

        Log.d(TAG, "Loading more comments; commentId: " + commentId
                + " - sort: " + sort + " - context: " + context);

        mAPI.getCommentThread(subreddit, article, commentId, sort, context, new Callback<List<ListingResponse>>() {
            @Override
            public void success(List<ListingResponse> listingsList, Response response) {
                BaseUtils.printResponseStatus(response);
                mBus.post(new CommentThreadLoadedEvent(listingsList, parentDepth));
            }

            @Override
            public void failure(RetrofitError error) {
                BaseUtils.showError(mContext, error);
                BaseUtils.printResponse(error.getResponse());
                mBus.post(new CommentThreadLoadedEvent(error));
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
