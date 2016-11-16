package com.ddiehl.android.htn.listings.profile;

import android.support.annotation.NonNull;

import com.ddiehl.android.htn.listings.ListingsView;
import com.ddiehl.android.htn.listings.comments.CommentView;
import com.ddiehl.android.htn.listings.links.LinkView;

import java.util.List;

import rxreddit.model.Listing;
import rxreddit.model.UserIdentity;

public interface UserProfileView extends ListingsView, LinkView, CommentView {

    void showUserInfo(@NonNull UserIdentity user);

    void showFriendNote(@NonNull String note);

    void hideFriendNote();

    void showTrophies(List<Listing> trophies);

    void setFriendButtonState(boolean isFriend);

    void selectTab(String show);

    String getShow();

    String getUsernameContext();

    String getSort();

    String getTimespan();
}
