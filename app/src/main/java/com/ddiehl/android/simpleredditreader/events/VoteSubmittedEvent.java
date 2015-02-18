package com.ddiehl.android.simpleredditreader.events;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class VoteSubmittedEvent {
    private Response mResponse;
    private RetrofitError mRetrofitError;
    private boolean mFailed = false;

    public VoteSubmittedEvent(Response response) {
        mResponse = response;
    }

    public VoteSubmittedEvent(RetrofitError error) {
        mRetrofitError = error;
        mFailed = true;
    }

    public Response getResponse() {
        return mResponse;
    }

    public RetrofitError getError() {
        return mRetrofitError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}
