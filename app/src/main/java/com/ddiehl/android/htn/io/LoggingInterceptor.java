/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.io;

import com.ddiehl.android.htn.BusProvider;
import com.ddiehl.android.htn.utils.BaseUtils;
import com.orhanobut.logger.Logger;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

class LoggingInterceptor implements Interceptor {
    private static final String TAG = LoggingInterceptor.class.getSimpleName();

    @Override
    public Response intercept(Chain chain) throws IOException {
        try {
            Request request = chain.request();
            Response response = chain.proceed(request);
            if (response != null) BaseUtils.printResponseStatus(response);
            return response;
        } catch (Exception e) {
            Logger.w("Exception occurred while sending HTTP request");
            Observable.just(null)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((action) -> {
                        BusProvider.getInstance().post(e);
                    });
        }
        return null;
    }
}
