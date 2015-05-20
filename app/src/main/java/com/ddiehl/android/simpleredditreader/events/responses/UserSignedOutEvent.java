package com.ddiehl.android.simpleredditreader.events.responses;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class UserSignedOutEvent {
    private Response mResponse;
    private RetrofitError mRetrofitError;
    private boolean mFailed = false;

    public UserSignedOutEvent(Response response) {
        mResponse = response;
    }

    public UserSignedOutEvent(RetrofitError error) {
        mRetrofitError = error;
        mFailed = true;
    }

    public Response getResponse() {
        return mResponse;
    }

    public RetrofitError getRetrofitError() {
        return mRetrofitError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}
