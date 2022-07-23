package com.ddiehl.android.htn.listings.profile;

import com.ddiehl.android.htn.listings.ListingsView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import rxreddit.model.Listing;
import rxreddit.model.UserIdentity;

public interface UserProfileView extends ListingsView {

    void showUserInfo(@NotNull UserIdentity user);

    void showFriendNote(@NotNull String note);

    void hideFriendNote();

    void showTrophies(List<Listing> trophies);

    void setFriendButtonState(boolean isFriend);

    void selectTab(String show);

    String getShow();

    String getUsernameContext();

    String getSort();

    String getTimespan();
}
