/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.io;

import android.util.Log;

import com.ddiehl.android.htn.BusProvider;
import com.ddiehl.android.htn.utils.BaseUtils;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

class LoggingInterceptor implements Interceptor {
    private static final String TAG = LoggingInterceptor.class.getSimpleName();

    @Override
    public Response intercept(Chain chain) throws IOException {
        try {

            Request request = chain.request();
            Response response = chain.proceed(request);
            BaseUtils.printResponseStatus(response);
            return response;
        } catch (Exception e) {
            Log.e(TAG, "Exception occurred while sending HTTP request", e);
            BusProvider.getInstance().post(e);
        }
        return null;
    }
}
