package com.ddiehl.android.htn.io;


import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.reddit.identity.AccessToken;
import com.ddiehl.reddit.identity.AuthorizationResponse;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.ResponseBody;

import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

public class RedditAuthServiceImpl implements RedditAuthService {

    private RedditAuthAPI mAuthService = buildApi();

    private RedditAuthAPI buildApi() {
        OkHttpClient client = new OkHttpClient();
        client.networkInterceptors().add(new UserAgentInterceptor(RedditService.USER_AGENT));
        client.networkInterceptors().add((chain) -> {
            Request originalRequest = chain.request();
            Request newRequest = originalRequest.newBuilder()
                    .removeHeader("Authorization")
                    .addHeader("Authorization", HTTP_AUTH_HEADER)
                    .build();
            return chain.proceed(newRequest);
        });
        client.networkInterceptors().add(new LoggingInterceptor());
        client.networkInterceptors().add(new StethoInterceptor());

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
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
    public Observable<AuthorizationResponse> authorizeApplication() {
        String grantType = "https://oauth.reddit.com/grants/installed_client";
        String deviceId = HoldTheNarwhal.getSettingsManager().getDeviceId();
        return mAuthService.getApplicationAuthToken(grantType, deviceId)
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .map(Response::body);
    }

    @Override
    public Observable<AuthorizationResponse> getUserAccessToken(
            String grantType, String authCode, String redirectUri) {
        return mAuthService.getUserAuthToken(grantType, authCode, redirectUri)
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .map(Response::body);
    }

    @Override
    public Observable<AuthorizationResponse> refreshUserAccessToken(String refreshToken) {
        String grantType = "refresh_token";
        return mAuthService.refreshUserAuthToken(grantType, refreshToken)
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .map(Response::body);
    }

    @Override
    public Observable<ResponseBody> revokeAuthToken(AccessToken token) {
        return Observable.merge(
                mAuthService.revokeUserAuthToken(token.getToken(), "access_token"),
                mAuthService.revokeUserAuthToken(token.getRefreshToken(), "refresh_token"))
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .map(Response::body);
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
