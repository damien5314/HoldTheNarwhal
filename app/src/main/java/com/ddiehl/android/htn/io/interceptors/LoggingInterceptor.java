package com.ddiehl.android.htn.io.interceptors;

import com.ddiehl.android.htn.utils.BaseUtils;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class LoggingInterceptor implements Interceptor {
  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    Response response = chain.proceed(request);
    if (response != null) BaseUtils.printResponseStatus(response);
    return response;
  }
}
