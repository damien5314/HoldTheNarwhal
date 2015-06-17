package com.ddiehl.android.simpleredditreader.io;

import android.content.Context;
import android.widget.Toast;

import com.ddiehl.android.simpleredditreader.BusProvider;
import com.ddiehl.android.simpleredditreader.RedditIdentityManager;
import com.ddiehl.android.simpleredditreader.events.exceptions.UserRequiredException;
import com.ddiehl.android.simpleredditreader.events.requests.GetUserIdentityEvent;
import com.ddiehl.android.simpleredditreader.events.requests.HideEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadLinkCommentsEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadMoreChildrenEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadSubredditEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadUserProfileEvent;
import com.ddiehl.android.simpleredditreader.events.requests.ReportEvent;
import com.ddiehl.android.simpleredditreader.events.requests.SaveEvent;
import com.ddiehl.android.simpleredditreader.events.requests.VoteEvent;
import com.ddiehl.android.simpleredditreader.events.responses.HideSubmittedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.LinkCommentsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.ListingsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.MoreChildrenLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.SaveSubmittedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.UserIdentityRetrievedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.VoteSubmittedEvent;
import com.ddiehl.android.simpleredditreader.utils.BaseUtils;
import com.ddiehl.reddit.Hideable;
import com.ddiehl.reddit.Savable;
import com.ddiehl.reddit.Votable;
import com.ddiehl.reddit.adapters.AbsRedditCommentDeserializer;
import com.ddiehl.reddit.adapters.ListingDeserializer;
import com.ddiehl.reddit.adapters.ListingResponseDeserializer;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.AbsRedditComment;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.ListingResponse;
import com.ddiehl.reddit.listings.MoreChildrenResponse;
import com.ddiehl.reddit.listings.RedditLink;
import com.ddiehl.reddit.listings.RedditMoreComments;
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

public class RedditServiceAPI implements RedditService {
    private static final String TAG = RedditServiceAPI.class.getSimpleName();

    private Context mContext;
    private Bus mBus;
    private RedditAPI mAPI;
    private RedditIdentityManager mIdentityManager;

    protected RedditServiceAPI(Context context, RedditIdentityManager manager) {
        mContext = context.getApplicationContext();
        mBus = BusProvider.getInstance();
        mBus.register(this);
        mAPI = buildApi();
        mIdentityManager = manager;
    }

    private RedditAPI buildApi() {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(ListingResponse.class, new ListingResponseDeserializer())
                .registerTypeAdapter(Listing.class, new ListingDeserializer())
                .registerTypeAdapter(AbsRedditComment.class, new AbsRedditCommentDeserializer())
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT_AUTHORIZED)
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addHeader("User-Agent", RedditService.USER_AGENT);
                        request.addHeader("Authorization", "bearer " + getAccessToken());
                        request.addHeader("Content-Length", "0");
                        request.addQueryParam("raw_json", "1");
                    }
                })
                .build();

        return restAdapter.create(RedditAPI.class);
    }

    private String getAccessToken() {
        if (mIdentityManager.hasValidUserAccessToken()) {
            return mIdentityManager.getUserAccessToken().getToken();
        } else if (mIdentityManager.hasValidApplicationAccessToken()) {
            return mIdentityManager.getApplicationAccessToken().getToken();
        }
        return null;
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

    @Subscribe
    public void onUserIdentityRetrieved(UserIdentityRetrievedEvent event) {
        if (event.isFailed()) {
            return;
        }

        Toast.makeText(mContext, "User identity retrieved", Toast.LENGTH_SHORT).show();
        UserIdentity id = event.getUserIdentity();
        mIdentityManager.saveUserIdentity(id);
    }

    /**
     * Retrieves link listings for subreddit
     */
    @Override
    public void onLoadLinks(LoadSubredditEvent event) {
        String subreddit = event.getSubreddit();
        String sort = event.getSort();
        String timespan = event.getTimeSpan();
        String after = event.getAfter();

        mAPI.getLinks(subreddit, sort, timespan, after, new Callback<ListingResponse>() {
            @Override
            public void success(ListingResponse linksResponse, Response response) {
                BaseUtils.printResponseStatus(response);
                mBus.post(new ListingsLoadedEvent(linksResponse));
            }

            @Override
            public void failure(RetrofitError error) {
                BaseUtils.showError(mContext, error);
                BaseUtils.printResponse(error.getResponse());
                mBus.post(new ListingsLoadedEvent(error));
            }
        });
    }

    /**
     * Retrieves comment listings for link passed as parameter
     */
    @Override
    public void onLoadLinkComments(LoadLinkCommentsEvent event) {
        String subreddit = event.getSubreddit();
        String article = event.getArticle();
        String sort = event.getSort();
        String commentId = event.getCommentId();

        mAPI.getComments(subreddit, article, sort, commentId, new Callback<List<ListingResponse>>() {
            @Override
            public void success(List<ListingResponse> listingsList, Response response) {
                BaseUtils.printResponseStatus(response);
                mBus.post(new LinkCommentsLoadedEvent(listingsList));
            }

            @Override
            public void failure(RetrofitError error) {
                BaseUtils.showError(mContext, error);
                BaseUtils.printResponse(error.getResponse());
                mBus.post(new LinkCommentsLoadedEvent(error));
            }
        });
    }

    /**
     * Retrieves more comments for link, with comment stub passed as parameter
     */
    @Override
    public void onLoadMoreChildren(LoadMoreChildrenEvent event) {
        RedditLink link = event.getRedditLink();
        final RedditMoreComments parentStub = event.getParentCommentStub();
        List<String> children = event.getChildren();
//        children = children.subList(0, Math.min(children.size(), 20));
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

    @Override
    public void onLoadUserProfile(LoadUserProfileEvent event) {
        final String show = event.getShow();
        final String userId = event.getUsername();
        final String sort = event.getSort();
        final String timespan = event.getTimeSpan();
        final String after = event.getAfter();

        mAPI.getUserProfile(show, userId, sort, timespan, after, new Callback<ListingResponse>() {
            @Override
            public void success(ListingResponse listing, Response response) {
                BaseUtils.printResponseStatus(response);
                mBus.post(new ListingsLoadedEvent(listing));
            }

            @Override
            public void failure(RetrofitError error) {
                BaseUtils.showError(mContext, error);
                BaseUtils.printResponse(error.getResponse());
                mBus.post(new ListingsLoadedEvent(error));
            }
        });
    }

    /**
     * Submits a vote on a link or comment
     */
    @Override
    public void onVote(final VoteEvent event) {
        final Votable listing = event.getListing();
        String fullname = String.format("%s_%s", event.getType(), listing.getId());

        mAPI.vote(fullname, event.getDirection(), new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                BaseUtils.printResponseStatus(response);

                try {
                    InputStream in = response.getBody().in();
                    if (BaseUtils.inputStreamToString(in).contains("USER_REQUIRED")) {
                        throw new UserRequiredException();
                    } else {
                        mBus.post(new VoteSubmittedEvent(listing, event.getDirection()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mBus.post(new VoteSubmittedEvent(e));
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

    /**
     * (un)Saves a link or comment
     */
    @Override
    public void onSave(final SaveEvent event) {
        final Savable listing = event.getListing();

        if (event.isToSave()) { // Save
            mAPI.save(listing.getName(), event.getCategory(), new Callback<Response>() {
                @Override
                public void success(Response response, Response response2) {
                    saveSuccess(response, listing, event.getCategory(), true);
                }

                @Override
                public void failure(RetrofitError error) {
                    BaseUtils.showError(mContext, error);
                    BaseUtils.printResponse(error.getResponse());
                    mBus.post(new SaveSubmittedEvent(error));
                }
            });
        } else { // Unsave
            mAPI.unsave(listing.getName(), new Callback<Response>() {
                @Override
                public void success(Response response, Response response2) {
                    saveSuccess(response, listing, event.getCategory(), false);
                }

                @Override
                public void failure(RetrofitError error) {
                    BaseUtils.showError(mContext, error);
                    BaseUtils.printResponse(error.getResponse());
                    mBus.post(new SaveSubmittedEvent(error));
                }
            });
        }
    }

    private void saveSuccess(Response response, Savable listing, String category, boolean toSave) {
        BaseUtils.printResponseStatus(response);

        try {
            InputStream in = response.getBody().in();
            if (BaseUtils.inputStreamToString(in).contains("USER_REQUIRED")) {
                throw new UserRequiredException();
            } else {
                mBus.post(new SaveSubmittedEvent(listing, category, toSave));
            }
        } catch (Exception e) {
            e.printStackTrace();
            mBus.post(new SaveSubmittedEvent(e));
        }
    }

    @Override
    public void onHide(final HideEvent event) {
        final Hideable listing = event.getListing();

        if (event.isToHide()) { // Hide
            mAPI.hide(listing.getName(), new Callback<Response>() {
                @Override
                public void success(Response response, Response response2) {
                    hideSuccess(response, listing, true);
                }

                @Override
                public void failure(RetrofitError error) {
                    BaseUtils.showError(mContext, error);
                    BaseUtils.printResponse(error.getResponse());
                    mBus.post(new HideSubmittedEvent(error));
                }
            });
        } else { // Unhide
            mAPI.unhide(listing.getName(), new Callback<Response>() {
                @Override
                public void success(Response response, Response response2) {
                    hideSuccess(response, listing, false);
                }

                @Override
                public void failure(RetrofitError error) {
                    BaseUtils.showError(mContext, error);
                    BaseUtils.printResponse(error.getResponse());
                    mBus.post(new HideSubmittedEvent(error));
                }
            });
        }
    }

    private void hideSuccess(Response response, Hideable listing, boolean toHide) {
        BaseUtils.printResponseStatus(response);

        try {
            InputStream in = response.getBody().in();
            if (BaseUtils.inputStreamToString(in).contains("USER_REQUIRED")) {
                throw new UserRequiredException();
            } else {
                mBus.post(new HideSubmittedEvent(listing, toHide));
            }
        } catch (Exception e) {
            e.printStackTrace();
            mBus.post(new HideSubmittedEvent(e));
        }
    }

    @Override
    public void onReport(final ReportEvent event) {

    }
}
