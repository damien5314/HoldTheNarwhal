package com.ddiehl.android.htn.events.responses;

public class UserAuthCodeReceivedEvent {

    private String mUserAuthCode;

    public UserAuthCodeReceivedEvent(String authCode) {
        mUserAuthCode = authCode;
    }

    public String getCode() {
        return mUserAuthCode;
    }
}
