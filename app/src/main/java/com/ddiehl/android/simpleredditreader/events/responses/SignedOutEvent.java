package com.ddiehl.android.simpleredditreader.events.responses;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class SignedOutEvent {
    private Response mResponse;
    private RetrofitError mRetrofitError;
    private boolean mFailed = false;

    public SignedOutEvent(Response response) {
        mResponse = response;
    }

    public SignedOutEvent(RetrofitError error) {
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
