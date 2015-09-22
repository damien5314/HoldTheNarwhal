package com.ddiehl.android.htn.events.responses;


import retrofit.Response;

public class UserProfileSummaryLoadedEvent extends FailableEvent {
    private Response mResponse;

    public UserProfileSummaryLoadedEvent(Response response) {
        mResponse = response;
    }

    public UserProfileSummaryLoadedEvent(Throwable e) {
        super(e);
    }

    public Response getResponse() {
        return mResponse;
    }
}
