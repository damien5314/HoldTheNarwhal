package com.ddiehl.android.htn.presenter;

import android.support.annotation.NonNull;

import rxreddit.model.PrivateMessage;
import rxreddit.model.UserIdentity;

public interface MessagePresenter {

    UserIdentity getUserIdentity();

    void replyToMessage(@NonNull PrivateMessage message);

    void markMessageRead(@NonNull PrivateMessage message);

    void markMessageUnread(@NonNull PrivateMessage message);

    void showMessagePermalink(@NonNull PrivateMessage message);

    void reportMessage(@NonNull PrivateMessage message);

    void blockUser(@NonNull PrivateMessage message);
}
