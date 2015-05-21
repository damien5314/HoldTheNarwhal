package com.ddiehl.reddit.identity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AuthorizationResponse {

    @Expose @SerializedName("access_token")
    protected String accessToken;

    @Expose @SerializedName("token_type")
    protected String tokenType;

    @Expose @SerializedName("expires_in")
    protected long expiresIn; // seconds

    @Expose @SerializedName("scope")
    protected String scope;

    @Expose @SerializedName("refresh_token")
    protected String refreshToken;

    public String getToken() {
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

    public String getRefreshToken() {
        return refreshToken;
    }
}
