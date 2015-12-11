package com.ddiehl.android.htn.io;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.ddiehl.android.htn.BuildConfig;
import com.ddiehl.android.htn.events.requests.FriendNoteSaveEvent;
import com.ddiehl.android.htn.events.requests.GetSubredditInfoEvent;
import com.ddiehl.android.htn.events.requests.HideEvent;
import com.ddiehl.android.htn.events.requests.ReportEvent;
import com.ddiehl.android.htn.events.requests.SaveEvent;
import com.ddiehl.android.htn.events.requests.VoteEvent;
import com.ddiehl.reddit.identity.FriendInfo;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.identity.UserSettings;
import com.ddiehl.reddit.listings.CommentStub;
import com.ddiehl.reddit.listings.Link;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.ListingResponse;
import com.ddiehl.reddit.listings.MoreChildrenResponse;
import com.squareup.okhttp.ResponseBody;

import java.util.List;
import java.util.Map;

import rx.Observable;


public interface RedditService {
    String USER_AGENT = String.format(
            "android:com.ddiehl.android.htn:v%s (by /u/damien5314)",
            BuildConfig.VERSION_NAME);
    String ENDPOINT_NORMAL = "https://www.reddit.com";
    String ENDPOINT_OAUTH = "https://oauth.reddit.com";

    Observable<UserIdentity> getUserIdentity();
    Observable<UserSettings> getUserSettings();
    Observable<ResponseBody> updateUserSettings(Map<String, String> settings);
    Observable<ListingResponse> loadLinks(
            @Nullable String subreddit, @Nullable String sort,
            @Nullable String timespan, @Nullable String after);
    Observable<List<ListingResponse>> loadLinkComments(
            @NonNull String subreddit, @NonNull String article,
            @Nullable String sort, @Nullable String commentId);
    Observable<Pair<CommentStub, MoreChildrenResponse>> loadMoreChildren(
            @NonNull Link link, @NonNull CommentStub moreComments,
            @NonNull List<String> children, @Nullable String sort);
    Observable<UserIdentity> getUserInfo(@NonNull String username);
    Observable<FriendInfo> getFriendInfo(String username);
    Observable<List<Listing>> getUserTrophies(@NonNull String username);
    Observable<ListingResponse> loadUserProfile(
            @NonNull String show, @NonNull String username, @Nullable String sort,
            @Nullable String timespan, @Nullable String after);
    Observable<ResponseBody> addFriend(@NonNull String username);
    Observable<ResponseBody> deleteFriend(@NonNull String username);
    void onSaveFriendNote(@NonNull FriendNoteSaveEvent event);
    void onGetSubredditInfo(@NonNull GetSubredditInfoEvent event);
    void onVote(@NonNull VoteEvent event);
    void onSave(@NonNull SaveEvent event);
    void onHide(@NonNull HideEvent event);
    void onReport(@NonNull ReportEvent event);
}
