package com.ddiehl.android.htn.view;

import android.support.annotation.NonNull;

import com.ddiehl.reddit.identity.FriendInfo;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.Listing;

import java.util.List;

public interface UserProfileSummaryView {
    void showUserInfo(@NonNull UserIdentity user);
    void showFriendInfo(FriendInfo friend);
    void showTrophies(List<Listing> trophies);
}
