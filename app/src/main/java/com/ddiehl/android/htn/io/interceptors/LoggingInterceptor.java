package com.ddiehl.android.htn.io.interceptors;

import com.ddiehl.android.htn.utils.AndroidUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class LoggingInterceptor implements Interceptor {
  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    Response response = chain.proceed(request);
    if (response != null) AndroidUtils.printResponseStatus(response);
    return response;
  }
}
