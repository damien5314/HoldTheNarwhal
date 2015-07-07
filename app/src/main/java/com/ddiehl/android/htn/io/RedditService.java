/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.io;

import com.ddiehl.android.htn.BuildConfig;
import com.ddiehl.android.htn.events.requests.GetUserSettingsEvent;
import com.ddiehl.android.htn.events.requests.HideEvent;
import com.ddiehl.android.htn.events.requests.LoadLinkCommentsEvent;
import com.ddiehl.android.htn.events.requests.LoadSubredditEvent;
import com.ddiehl.android.htn.events.requests.LoadMoreChildrenEvent;
import com.ddiehl.android.htn.events.requests.LoadUserProfileEvent;
import com.ddiehl.android.htn.events.requests.ReportEvent;
import com.ddiehl.android.htn.events.requests.SaveEvent;
import com.ddiehl.android.htn.events.requests.UpdateUserSettingsEvent;
import com.ddiehl.android.htn.events.requests.VoteEvent;


public interface RedditService {

    String USER_AGENT = String.format("android:com.ddiehl.android.htn:v%s (by /u/damien5314)", BuildConfig.VERSION_NAME);
    String ENDPOINT_NORMAL = "https://www.reddit.com";
    String ENDPOINT_AUTHORIZED = "https://oauth.reddit.com";

    void onLoadLinks(LoadSubredditEvent event);
    void onLoadLinkComments(LoadLinkCommentsEvent event);
    void onLoadMoreChildren(LoadMoreChildrenEvent event);
    void onLoadUserProfile(LoadUserProfileEvent event);

    void onVote(VoteEvent event);
    void onSave(SaveEvent event);
    void onHide(HideEvent event);
    void onReport(ReportEvent event);
    void onGetUserSettings(GetUserSettingsEvent event);
    void onUpdateUserSettings(UpdateUserSettingsEvent event);
}
