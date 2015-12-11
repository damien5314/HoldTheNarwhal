package com.ddiehl.android.htn.io.interceptors;

import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class RawResponseInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        HttpUrl url = originalRequest.httpUrl().newBuilder()
                .addQueryParameter("raw_json", "1").build();
        Request newRequest = originalRequest.newBuilder()
                .url(url)
                .build();
        return chain.proceed(newRequest);
    }
}
