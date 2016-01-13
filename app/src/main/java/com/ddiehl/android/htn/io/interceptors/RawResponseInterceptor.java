package com.ddiehl.android.htn.io.interceptors;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RawResponseInterceptor implements Interceptor {
  @Override
  public Response intercept(Chain chain) throws IOException {
    Request originalRequest = chain.request();
    HttpUrl url = originalRequest.url().newBuilder()
        .addQueryParameter("raw_json", "1").build();
    Request newRequest = originalRequest.newBuilder()
        .url(url)
        .build();
    return chain.proceed(newRequest);
  }
}
