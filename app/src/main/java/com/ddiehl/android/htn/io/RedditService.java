package com.ddiehl.android.htn.io;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ddiehl.android.htn.BuildConfig;
import com.ddiehl.reddit.Hideable;
import com.ddiehl.reddit.Savable;
import com.ddiehl.reddit.Votable;
import com.ddiehl.reddit.identity.FriendInfo;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.identity.UserSettings;
import com.ddiehl.reddit.listings.CommentStub;
import com.ddiehl.reddit.listings.Link;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.ListingResponse;
import com.ddiehl.reddit.listings.MoreChildrenResponse;
import com.ddiehl.reddit.listings.Subreddit;
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
  Observable<MoreChildrenResponse> loadMoreChildren(
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
  Observable<ResponseBody> saveFriendNote(@NonNull String username, @NonNull String note);
  Observable<Subreddit> getSubredditInfo(@NonNull String subreddit);
  Observable<ResponseBody> vote(@NonNull Votable votable, int direction);
  Observable<ResponseBody> save(@NonNull Savable listing, @Nullable String category, boolean save);
  Observable<ResponseBody> hide(@NonNull Hideable listing, boolean toHide);
  Observable<ResponseBody> report(@NonNull String id, @NonNull String reason);
  Observable<ResponseBody> addComment(@NonNull Listing listing, @NonNull String text);
}
