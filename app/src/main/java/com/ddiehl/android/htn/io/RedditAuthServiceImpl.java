package com.ddiehl.android.htn.io;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.io.interceptors.AuthorizationInterceptor;
import com.ddiehl.android.htn.io.interceptors.LoggingInterceptor;
import com.ddiehl.android.htn.io.interceptors.UserAgentInterceptor;
import com.ddiehl.android.htn.utils.AndroidUtils;
import com.ddiehl.reddit.identity.AccessToken;
import com.ddiehl.reddit.identity.ApplicationAccessToken;
import com.ddiehl.reddit.identity.UserAccessToken;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

public class RedditAuthServiceImpl implements RedditAuthService {
  private RedditAuthAPI mAuthService = buildApi();

  private RedditAuthAPI buildApi() {
    OkHttpClient client = new OkHttpClient.Builder()
        .addNetworkInterceptor(new UserAgentInterceptor(RedditService.USER_AGENT))
        .addNetworkInterceptor(
            AuthorizationInterceptor.get(AuthorizationInterceptor.Type.HTTP_AUTH))
        .addNetworkInterceptor(new LoggingInterceptor())
//        .addNetworkInterceptor(new StethoInterceptor())
        .build();

    Gson gson = new GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .excludeFieldsWithoutExposeAnnotation()
        .create();

    Retrofit restAdapter = new Retrofit.Builder()
        .client(client)
        .baseUrl(ENDPOINT_NORMAL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .build();

    return restAdapter.create(RedditAuthAPI.class);
  }

  @Override
  public Observable<ApplicationAccessToken> authorizeApplication() {
    if (!connectedToNetwork()) return Observable.error(new NetworkUnavailableException());
    String grantType = "https://oauth.reddit.com/grants/installed_client";
    String deviceId = HoldTheNarwhal.getSettingsManager().getDeviceId();
    return mAuthService.getApplicationAuthToken(grantType, deviceId)
        .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
        .map(Response::body);
  }

  @Override
  public Observable<UserAccessToken> getUserAccessToken(
      String grantType, String authCode, String redirectUri) {
    if (!connectedToNetwork()) return Observable.error(new NetworkUnavailableException());
    return mAuthService.getUserAuthToken(grantType, authCode, redirectUri)
        .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
        .map(Response::body);
  }

  @Override
  public Observable<UserAccessToken> refreshUserAccessToken(String refreshToken) {
    if (!connectedToNetwork()) return Observable.error(new NetworkUnavailableException());
    String grantType = "refresh_token";
    return mAuthService.refreshUserAuthToken(grantType, refreshToken)
        .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
        .map(Response::body);
  }

  @Override
  public Observable<ResponseBody> revokeAuthToken(AccessToken token) {
    if (!connectedToNetwork()) return Observable.error(new NetworkUnavailableException());
    return Observable.merge(
        mAuthService.revokeUserAuthToken(token.getToken(), "access_token"),
        mAuthService.revokeUserAuthToken(token.getRefreshToken(), "refresh_token"))
        .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
        .map(Response::body);
  }

  private boolean connectedToNetwork() {
    return AndroidUtils.isConnectedToNetwork(
        HoldTheNarwhal.getContext());
  }

  ///////////////
  // Singleton //
  ///////////////

  private static RedditAuthService _instance;

  private RedditAuthServiceImpl() { }

  public static RedditAuthService getInstance() {
    if (_instance == null) {
      synchronized (RedditAuthServiceImpl.class) {
        if (_instance == null) {
          _instance = new RedditAuthServiceImpl();
        }
      }
    }
    return _instance;
  }
}
