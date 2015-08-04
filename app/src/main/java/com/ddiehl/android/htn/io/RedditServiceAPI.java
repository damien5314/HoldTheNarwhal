/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.io;

import android.content.Context;

import com.ddiehl.android.htn.AccessTokenManager;
import com.ddiehl.android.htn.BusProvider;
import com.ddiehl.android.htn.events.requests.GetUserIdentityEvent;
import com.ddiehl.android.htn.events.requests.GetUserSettingsEvent;
import com.ddiehl.android.htn.events.requests.HideEvent;
import com.ddiehl.android.htn.events.requests.LoadLinkCommentsEvent;
import com.ddiehl.android.htn.events.requests.LoadMoreChildrenEvent;
import com.ddiehl.android.htn.events.requests.LoadSubredditEvent;
import com.ddiehl.android.htn.events.requests.LoadUserProfileListingEvent;
import com.ddiehl.android.htn.events.requests.LoadUserProfileSummaryEvent;
import com.ddiehl.android.htn.events.requests.ReportEvent;
import com.ddiehl.android.htn.events.requests.SaveEvent;
import com.ddiehl.android.htn.events.requests.UpdateUserSettingsEvent;
import com.ddiehl.android.htn.events.requests.VoteEvent;
import com.ddiehl.android.htn.events.responses.FriendInfoLoadedEvent;
import com.ddiehl.android.htn.events.responses.HideSubmittedEvent;
import com.ddiehl.android.htn.events.responses.LinkCommentsLoadedEvent;
import com.ddiehl.android.htn.events.responses.ListingsLoadedEvent;
import com.ddiehl.android.htn.events.responses.MoreChildrenLoadedEvent;
import com.ddiehl.android.htn.events.responses.SaveSubmittedEvent;
import com.ddiehl.android.htn.events.responses.TrophiesLoadedEvent;
import com.ddiehl.android.htn.events.responses.UserIdentityRetrievedEvent;
import com.ddiehl.android.htn.events.responses.UserInfoLoadedEvent;
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
import com.ddiehl.reddit.listings.AbsComment;
import com.ddiehl.reddit.listings.CommentStub;
import com.ddiehl.reddit.listings.Link;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.ListingResponse;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Bus;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedString;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class RedditServiceAPI implements RedditService {
    private static final String TAG = RedditServiceAPI.class.getSimpleName();

    private Context mContext;
    private Bus mBus = BusProvider.getInstance();
    private RedditAPI mAPI;
    private AccessTokenManager mAccessTokenManager;

    RedditServiceAPI(Context c) {
        mContext = c.getApplicationContext();
        mAPI = buildApi();
        mAccessTokenManager = AccessTokenManager.getInstance(mContext);
    }

    private RedditAPI buildApi() {
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        OkHttpClient client = new OkHttpClient()
                .setCache(new Cache(
                        new File(mContext.getCacheDir().getAbsolutePath(), "htn-http-cache"),
                        cacheSize));
        client.networkInterceptors().add(new StethoInterceptor());
        client.networkInterceptors().add(new LoggingInterceptor());

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(ListingResponse.class, new ListingResponseDeserializer())
                .registerTypeAdapter(Listing.class, new ListingDeserializer())
                .registerTypeAdapter(AbsComment.class, new AbsCommentDeserializer())
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(new OkClient(client))
                .setEndpoint(ENDPOINT_AUTHORIZED)
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(request -> {
                    request.addHeader("User-Agent", RedditService.USER_AGENT);
                    request.addHeader("Authorization", "bearer " + getAccessToken());
                    request.addQueryParam("raw_json", "1");
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
        mAPI.getUserIdentity()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        userIdentity -> mBus.post(new UserIdentityRetrievedEvent(userIdentity)),
                        error -> {
                            mBus.post(error);
                            mBus.post(new UserIdentityRetrievedEvent(error));
                        });
    }

    @Override
    public void onGetUserSettings(GetUserSettingsEvent event) {
        mAPI.getUserSettings()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        settings -> mBus.post(new UserSettingsRetrievedEvent(settings)),
                        error -> {
                            mBus.post(error);
                            mBus.post(new UserSettingsRetrievedEvent(error));
                        });
    }

    @Override
    public void onUpdateUserSettings(UpdateUserSettingsEvent event) {
        String json = new Gson().toJson(event.getPrefs());
        mAPI.updateUserSettings(new TypedString(json))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> mBus.post(new UserSettingsUpdatedEvent()),
                        error -> {
                            mBus.post(error);
                            mBus.post(new UserSettingsUpdatedEvent(error));
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

        mAPI.getLinks(subreddit, sort, timespan, after)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        linksResponse -> mBus.post(new ListingsLoadedEvent(linksResponse)),
                        error -> {
                            mBus.post(error);
                            mBus.post(new ListingsLoadedEvent(error));
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

        mAPI.getComments(subreddit, article, sort, commentId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        listingsList -> mBus.post(new LinkCommentsLoadedEvent(listingsList)),
                        error -> {
                            mBus.post(error);
                            mBus.post(new LinkCommentsLoadedEvent(error));
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

        mAPI.getMoreChildren(link.getName(), childrenString, sort)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> mBus.post(new MoreChildrenLoadedEvent(parentStub, response)),
                        error -> {
                            mBus.post(error);
                            mBus.post(new MoreChildrenLoadedEvent(error));
                        });
    }

    @Override
    public void onLoadUserProfileSummary(LoadUserProfileSummaryEvent event) {
        final String username = event.getUsername();

        // getUserInfo for friend status, karma, create date
        mAPI.getUserInfo(username)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        listing -> {
                            UserIdentity user = listing.getUser();
                            mBus.post(new UserInfoLoadedEvent(user));
                            if (user.isFriend()) {
                                // FIXME Make this proper functional style
                                // getFriendInfo for friend note
                                mAPI.getFriendInfo(username)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(
                                                response -> mBus.post(new FriendInfoLoadedEvent(response)),
                                                error -> {
                                                    mBus.post(error);
                                                    mBus.post(new FriendInfoLoadedEvent(error));
                                                }
                                        );
                            }
                        },
                        error -> {
                            mBus.post(error);
                            mBus.post(new UserInfoLoadedEvent(error));
                        }
                );

        // getUserTrophies for user trophies
        mAPI.getUserTrophies(username)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> mBus.post(new TrophiesLoadedEvent(response)),
                        error -> {
                            mBus.post(error);
                            mBus.post(new TrophiesLoadedEvent(error));
                        }
                );
    }

    @Override
    public void onLoadUserProfile(LoadUserProfileListingEvent event) {
        final String show = event.getShow();
        final String userId = event.getUsername();
        final String sort = event.getSort();
        final String timespan = event.getTimeSpan();
        final String after = event.getAfter();

        mAPI.getUserProfile(show, userId, sort, timespan, after)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        listing -> mBus.post(new ListingsLoadedEvent(listing)),
                        error -> {
                            mBus.post(error);
                            mBus.post(new ListingsLoadedEvent(error));
                        });
    }

    /**
     * Submits a vote on a link or comment
     */
    @Override
    public void onVote(final VoteEvent event) {
        final Votable listing = event.getListing();
        String fullname = String.format("%s_%s", event.getType(), listing.getId());

        mAPI.vote("", fullname, event.getDirection())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {

                            try {
                                InputStream in = response.getBody().in();
                                if (!BaseUtils.getStringFromInputStream(in).contains("USER_REQUIRED")) {
                                    mBus.post(new VoteSubmittedEvent(listing, event.getDirection()));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                mBus.post(new VoteSubmittedEvent(e));
                            }
                        },
                        error -> {
                            mBus.post(error);
                            mBus.post(new VoteSubmittedEvent(error));
                        });
    }

    /**
     * (un)Saves a link or comment
     */
    @Override
    public void onSave(final SaveEvent event) {
        final Savable listing = event.getListing();
        final String category = event.getCategory();
        final boolean toSave = event.isToSave();

        Action1<Response> onSaveSuccess = (response) -> {
            try {
                InputStream in = response.getBody().in();
                if (!BaseUtils.getStringFromInputStream(in).contains("USER_REQUIRED")) {
                    mBus.post(new SaveSubmittedEvent(listing, category, toSave));
                }
            } catch (Exception e) {
                e.printStackTrace();
                mBus.post(new SaveSubmittedEvent(e));
            }
        };

        Action1<Throwable> onSaveFailure = (error) -> {
            mBus.post(error);
            mBus.post(new SaveSubmittedEvent(error));
        };

        if (event.isToSave()) { // Save
            mAPI.save("", listing.getName(), event.getCategory())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(onSaveSuccess, onSaveFailure);
        } else { // Unsave
            mAPI.unsave("", listing.getName())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(onSaveSuccess, onSaveFailure);
        }
    }

    @Override
    public void onHide(final HideEvent event) {
        final Hideable listing = event.getListing();
        final boolean toHide = event.isToHide();

        Action1<Response> onSuccess = (response) -> {
            try {
                InputStream in = response.getBody().in();
                if (!BaseUtils.getStringFromInputStream(in).contains("USER_REQUIRED")) {
                    mBus.post(new HideSubmittedEvent(listing, toHide));
                }
            } catch (Exception e) {
                e.printStackTrace();
                mBus.post(new HideSubmittedEvent(e));
            }
        };

        Action1<Throwable> onFailure = (error) -> {
            mBus.post(error);
            mBus.post(new HideSubmittedEvent(error));
        };

        if (event.isToHide()) { // Hide
            mAPI.hide("", listing.getName())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(onSuccess, onFailure);
        } else { // Unhide
            mAPI.unhide("", listing.getName())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(onSuccess, onFailure);
        }
    }

    @Override
    public void onReport(final ReportEvent event) {

    }
}
