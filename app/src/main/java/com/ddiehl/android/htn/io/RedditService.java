/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.io;

import android.support.annotation.NonNull;

import com.ddiehl.android.htn.BuildConfig;
import com.ddiehl.android.htn.events.requests.FriendAddEvent;
import com.ddiehl.android.htn.events.requests.FriendDeleteEvent;
import com.ddiehl.android.htn.events.requests.FriendNoteSaveEvent;
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
import com.ddiehl.android.htn.events.requests.GetSubredditInfoEvent;
import com.ddiehl.android.htn.events.requests.VoteEvent;


public interface RedditService {

    String USER_AGENT = String.format("android:com.ddiehl.android.htn:v%s (by /u/damien5314)", BuildConfig.VERSION_NAME);
    String ENDPOINT_NORMAL = "https://www.reddit.com";
    String ENDPOINT_AUTHORIZED = "https://oauth.reddit.com";

    void onLoadLinks(@NonNull LoadSubredditEvent event);
    void onLoadLinkComments(@NonNull LoadLinkCommentsEvent event);
    void onLoadMoreChildren(@NonNull LoadMoreChildrenEvent event);
    void onLoadUserProfileSummary(@NonNull LoadUserProfileSummaryEvent event);
    void onLoadUserProfile(@NonNull LoadUserProfileListingEvent event);

    void onGetSubredditInfo(@NonNull GetSubredditInfoEvent event);
    void onGetUserIdentity(@NonNull GetUserIdentityEvent event);
    void onGetUserSettings(@NonNull GetUserSettingsEvent event);
    void onUpdateUserSettings(@NonNull UpdateUserSettingsEvent event);
    void onVote(@NonNull VoteEvent event);
    void onSave(@NonNull SaveEvent event);
    void onHide(@NonNull HideEvent event);
    void onReport(@NonNull ReportEvent event);

    void onAddFriend(@NonNull FriendAddEvent event);
    void onDeleteFriend(@NonNull FriendDeleteEvent event);
    void onSaveFriendNote(@NonNull FriendNoteSaveEvent event);
}
