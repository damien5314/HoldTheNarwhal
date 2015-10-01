package com.ddiehl.android.htn.events.responses;

import android.support.annotation.NonNull;

public class UserAuthCodeReceivedEvent {

    private String mUserAuthCode;

    public UserAuthCodeReceivedEvent(@NonNull String authCode) {
        mUserAuthCode = authCode;
    }

    @NonNull
    public String getCode() {
        return mUserAuthCode;
    }
}
