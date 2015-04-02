package com.ddiehl.android.simpleredditreader.web;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AuthTokenResponse {

// Response format
//{
//    "access_token": Your access token,
//    "token_type": "bearer",
//    "expires_in": Unix Epoch Seconds,
//    "scope": A scope string,
//}

    @Expose @SerializedName("access_token")
    private String accessToken;

    @Expose @SerializedName("token_type")
    private String tokenType;

    @Expose @SerializedName("expires_in")
    private long expiresIn;

    @Expose
    private String scope;

    public String getAuthToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public String getScope() {
        return scope;
    }
}
