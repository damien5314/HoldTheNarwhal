/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.io;

import android.content.Context;

import com.ddiehl.android.htn.AccessTokenManager;
import com.ddiehl.android.htn.BusProvider;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.SettingsManager;
import com.ddiehl.android.htn.events.requests.GetUserIdentityEvent;
import com.ddiehl.android.htn.events.requests.GetUserSettingsEvent;
import com.ddiehl.android.htn.events.requests.HideEvent;
import com.ddiehl.android.htn.events.requests.LoadLinkCommentsEvent;
import com.ddiehl.android.htn.events.requests.LoadMoreChildrenEvent;
import com.ddiehl.android.htn.events.requests.LoadSubredditEvent;
import com.ddiehl.android.htn.events.requests.LoadUserProfileEvent;
import com.ddiehl.android.htn.events.requests.ReportEvent;
import com.ddiehl.android.htn.events.requests.SaveEvent;
import com.ddiehl.android.htn.events.requests.UpdateUserSettingsEvent;
import com.ddiehl.android.htn.events.requests.VoteEvent;
import com.ddiehl.android.htn.events.responses.HideSubmittedEvent;
import com.ddiehl.android.htn.events.responses.LinkCommentsLoadedEvent;
import com.ddiehl.android.htn.events.responses.ListingsLoadedEvent;
import com.ddiehl.android.htn.events.responses.MoreChildrenLoadedEvent;
import com.ddiehl.android.htn.events.responses.SaveSubmittedEvent;
import com.ddiehl.android.htn.events.responses.UserIdentityRetrievedEvent;
import com.ddiehl.android.htn.events.responses.UserSettingsRetrievedEvent;
import com.ddiehl.android.htn.events.responses.UserSettingsUpdatedEvent;
import com.ddiehl.android.htn.events.responses.VoteSubmittedEvent;
import com.ddiehl.android.htn.utils.BaseUtils;
import com.ddiehl.reddit.Hideable;
import com.ddiehl.reddit.Savable;
import com.ddiehl.reddit.Votable;
import com.ddiehl.reddit.adapters.AbsCommentDeserializer;
import com.ddiehl.reddit.adapters.ListingDeserializer;
import com.ddiehl.reddit.adapters.ListingResponseDeserializer;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.identity.UserSettings;
import com.ddiehl.reddit.listings.AbsComment;
import com.ddiehl.reddit.listings.CommentStub;
import com.ddiehl.reddit.listings.Link;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.ListingResponse;
import com.ddiehl.reddit.listings.MoreChildrenResponse;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Bus;

import java.io.InputStream;
import java.util.List;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedString;

public class RedditServiceAPI implements RedditService {
    private static final String TAG = RedditServiceAPI.class.getSimpleName();

    private Context mContext;
    private Bus mBus = BusProvider.getInstance();
    private RedditAPI mAPI;
    private AccessTokenManager mAccessTokenManager;
    private IdentityManager mIdentityManager;
    private SettingsManager mSettingsManager;

    RedditServiceAPI(Context c) {
        mContext = c.getApplicationContext();
        mAPI = buildApi();
        mAccessTokenManager = AccessTokenManager.getInstance(mContext);
        mIdentityManager = IdentityManager.getInstance(mContext);
        mSettingsManager = SettingsManager.getInstance(mContext);
    }

    private RedditAPI buildApi() {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(ListingResponse.class, new ListingResponseDeserializer())
                .registerTypeAdapter(Listing.class, new ListingDeserializer())
                .registerTypeAdapter(AbsComment.class, new AbsCommentDeserializer())
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT_AUTHORIZED)
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addHeader("User-Agent", RedditService.USER_AGENT);
                        request.addHeader("Authorization", "bearer " + getAccessToken());
//                        request.addHeader("Content-Length", "0");
//                        request.addHeader("Content-Length", String.valueOf(request.toString().length())));
                        request.addQueryParam("raw_json", "1");
                    }
                })
                .build();

        return restAdapter.create(RedditAPI.class);
    }

    private String getAccessToken() {
        if (mAccessTokenManager.hasValidUserAccessToken()) {
            return mAccessTokenManager.getUserAccessToken().getToken();
        } else if (mAccessTokenManager.hasValidApplicationAccessToken()) {
            return mAccessTokenManager.getApplicationAccessToken().getToken();
        }
        return null;
    }

    @Override
    public void onGetUserIdentity(GetUserIdentityEvent event) {
        mAPI.getUserIdentity(new Callback<UserIdentity>() {
            @Override
            public void success(UserIdentity userIdentity, Response response) {
                BaseUtils.printResponseStatus(response);
                mBus.post(new UserIdentityRetrievedEvent(userIdentity));
            }

            @Override
            public void failure(RetrofitError error) {
                BaseUtils.printResponse(error.getResponse());
                mBus.post(error);
                mBus.post(new UserIdentityRetrievedEvent(error));
            }
        });
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
                BaseUtils.printResponse(error.getResponse());
                mBus.post(error);
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
                BaseUtils.printResponse(error.getResponse());
                mBus.post(error);
                mBus.post(new LinkCommentsLoadedEvent(error));
            }
        });
    }

    /**
     * Retrieves more comments for link, with comment stub passed as parameter
     */
    @Override
    public void onLoadMoreChildren(LoadMoreChildrenEvent event) {
        Link link = event.getLink();
        final CommentStub parentStub = event.getParentCommentStub();
        List<String> children = event.getChildren();
//        children = children.subList(0, Math.min(children.size(), 20));
        String sort = event.getSort();

        StringBuilder b = new StringBuilder();
        for (String child : children)
            b.append(child).append(",");
        String childrenString = b.toString();
        childrenString = childrenString.substring(0, Math.max(childrenString.length() - 1, 0));

        mAPI.getMoreChildren(link.getName(), childrenString, sort, new Callback<MoreChildrenResponse>() {
            @Override
            public void success(MoreChildrenResponse moreChildrenResponse, Response response) {
                BaseUtils.printResponseStatus(response);
                mBus.post(new MoreChildrenLoadedEvent(parentStub, moreChildrenResponse));
            }

            @Override
            public void failure(RetrofitError error) {
                BaseUtils.printResponse(error.getResponse());
                mBus.post(error);
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
                BaseUtils.printResponse(error.getResponse());
                mBus.post(error);
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
//                        throw new UserRequiredException();
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
                BaseUtils.printResponse(error.getResponse());
                mBus.post(error);
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
                    BaseUtils.printResponse(error.getResponse());
                    mBus.post(error);
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
                    BaseUtils.printResponse(error.getResponse());
                    mBus.post(error);
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
//                throw new UserRequiredException();
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
                    BaseUtils.printResponse(error.getResponse());
                    mBus.post(error);
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
                    BaseUtils.printResponse(error.getResponse());
                    mBus.post(error);
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
//                throw new UserRequiredException();
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

    @Override
    public void onGetUserSettings(GetUserSettingsEvent event) {
        mAPI.getUserSettings(new Callback<UserSettings>() {
            @Override
            public void success(UserSettings settings, Response response) {
                BaseUtils.printResponseStatus(response);
                mBus.post(new UserSettingsRetrievedEvent(settings));
            }

            @Override
            public void failure(RetrofitError error) {
                BaseUtils.printResponse(error.getResponse());
                mBus.post(error);
                mBus.post(new UserSettingsRetrievedEvent(error));
            }
        });
    }

    @Override
    public void onUpdateUserSettings(UpdateUserSettingsEvent event) {
        String json = new GsonBuilder().create().toJson(event.getPrefs());
        mAPI.updateUserSettings(new TypedString(json), new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                BaseUtils.printResponseStatus(response);
                mBus.post(new UserSettingsUpdatedEvent());
            }

            @Override
            public void failure(RetrofitError error) {
                BaseUtils.printResponse(error.getResponse());
                mBus.post(error);
                mBus.post(new UserSettingsUpdatedEvent(error));
            }
        });
    }
}
