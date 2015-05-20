package com.ddiehl.android.simpleredditreader.events.responses;

import retrofit.client.Response;

public class UserSignedOutEvent {
    private Response mResponse;
    private Exception mError;
    private boolean mFailed = false;

    public UserSignedOutEvent(Response response) {
        mResponse = response;
    }

    public UserSignedOutEvent(Exception error) {
        mError = error;
        mFailed = true;
    }

    public Response getResponse() {
        return mResponse;
    }

    public Exception getError() {
        return mError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}
