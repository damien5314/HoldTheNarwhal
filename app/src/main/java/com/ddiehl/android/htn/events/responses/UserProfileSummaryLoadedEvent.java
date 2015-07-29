/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.events.responses;

import retrofit.RetrofitError;
import retrofit.client.Response;


public class UserProfileSummaryLoadedEvent extends FailableEvent {
    private Response mResponse;

    public UserProfileSummaryLoadedEvent(Response response) {
        mResponse = response;
    }

    public UserProfileSummaryLoadedEvent(RetrofitError e) {
        super(e);
    }

    public Response getResponse() {
        return mResponse;
    }
}
