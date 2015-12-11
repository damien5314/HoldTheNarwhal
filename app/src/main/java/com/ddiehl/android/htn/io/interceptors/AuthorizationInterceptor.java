package com.ddiehl.android.htn.io.interceptors;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.io.RedditAuthService;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;

public class AuthorizationInterceptor {
  public enum Type { HTTP_AUTH, TOKEN_AUTH }

  private AuthorizationInterceptor() { }

  public static Interceptor get(Type t) {
    switch (t) {
      case HTTP_AUTH:
        return (chain) -> {
          Request originalRequest = chain.request();
          Request newRequest = originalRequest.newBuilder()
              .removeHeader("Authorization")
              .addHeader("Authorization", RedditAuthService.HTTP_AUTH_HEADER)
              .build();
          return chain.proceed(newRequest);
        };
      case TOKEN_AUTH:
        return chain -> {
          Request originalRequest = chain.request();
          Request newRequest = originalRequest.newBuilder()
              .removeHeader("Authorization")
              .addHeader("Authorization", "bearer "
                  + HoldTheNarwhal.getAccessTokenManager().getValidAccessToken())
              .build();
          return chain.proceed(newRequest);
        };
      default:
        /* no-op */
        return (chain) -> chain.proceed(chain.request());
    }
  }
}
