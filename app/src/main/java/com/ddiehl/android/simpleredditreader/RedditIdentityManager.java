package com.ddiehl.android.simpleredditreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.ddiehl.android.simpleredditreader.events.responses.UserIdentitySavedEvent;
import com.ddiehl.reddit.identity.AccessToken;
import com.ddiehl.reddit.identity.ApplicationAccessToken;
import com.ddiehl.reddit.identity.AuthorizationResponse;
import com.ddiehl.reddit.identity.UserAccessToken;
import com.ddiehl.reddit.identity.UserIdentity;
import com.squareup.otto.Bus;

import java.util.Date;

public class RedditIdentityManager {
    private static final String TAG = RedditIdentityManager.class.getSimpleName();

    private static final String PREFS_USER_ACCESS_TOKEN = "prefs_user_access_token";
    private static final String PREFS_APPLICATION_ACCESS_TOKEN = "prefs_application_access_token";
    private static final String PREF_ACCESS_TOKEN = "pref_access_token";
    private static final String PREF_TOKEN_TYPE = "pref_token_type";
    private static final String PREF_EXPIRATION = "pref_expiration";
    private static final String PREF_SCOPE = "pref_scope";
    private static final String PREF_REFRESH_TOKEN = "pref_refresh_token";

    private static final String PREFS_USER_IDENTITY = "user_identity";
    private static final String PREF_HAS_MAIL = "pref_has_mail";
    private static final String PREF_NAME = "pref_name";
    private static final String PREF_CREATED = "pref_created";
    private static final String PREF_HIDE_FROM_ROBOTS = "pref_hide_from_robots";
    private static final String PREF_GOLD_CREDDITS = "pref_gold_creddits";
    private static final String PREF_CREATED_UTC = "pref_created_utc";
    private static final String PREF_HAS_MOD_MAIL = "pref_has_mod_mail";
    private static final String PREF_LINK_KARMA = "pref_link_karma";
    private static final String PREF_COMMENT_KARMA = "pref_comment_karma";
    private static final String PREF_IS_OVER_18 = "pref_is_over_18";
    private static final String PREF_IS_GOLD = "pref_is_gold";
    private static final String PREF_IS_MOD = "pref_is_mod";
    private static final String PREF_GOLD_EXPIRATION = "pref_gold_expiration";
    private static final String PREF_HAS_VERIFIED_EMAIL = "pref_has_verified_email";
    private static final String PREF_ID = "pref_id";
    private static final String PREF_INBOX_COUNT = "pref_inbox_count";

    // Seconds within expiration we should try to retrieve a new auth token
    private static final int EXPIRATION_THRESHOLD = 60;

    private static RedditIdentityManager _instance;

    private Bus mBus;
    private Context mContext;
    
    private AccessToken mUserAccessToken;
    private AccessToken mApplicationAccessToken;
    private UserIdentity mUserIdentity;

    private RedditIdentityManager(Context context) {
        mBus = BusProvider.getInstance();
        mContext = context.getApplicationContext();
        mUserAccessToken = getSavedUserAccessToken();
        mApplicationAccessToken = getSavedApplicationAccessToken();
        mUserIdentity = getSavedUserIdentity();
    }

    public boolean hasValidUserAccessToken() {
        AccessToken token = getUserAccessToken();
        return token != null && token.secondsUntilExpiration() > EXPIRATION_THRESHOLD;
    }

    public boolean hasUserAccessRefreshToken() {
        AccessToken token = getUserAccessToken();
        return token != null && token.hasRefreshToken();
    }

    public boolean hasValidApplicationAccessToken() {
        AccessToken token = getApplicationAccessToken();
        return token != null && token.secondsUntilExpiration() > EXPIRATION_THRESHOLD;
    }

    public boolean hasValidAccessToken() {
        return hasValidUserAccessToken() || hasValidApplicationAccessToken();
    }

    public AccessToken getUserAccessToken() {
        if (mUserAccessToken == null) {
            mUserAccessToken = getSavedUserAccessToken();
        }
        return mUserAccessToken;
    }

    public AccessToken getApplicationAccessToken() {
        if (mApplicationAccessToken == null) {
            mApplicationAccessToken = getSavedApplicationAccessToken();
        }
        return mApplicationAccessToken;
    }

    public AccessToken getSavedUserAccessToken() {
        SharedPreferences sp =  mContext.getSharedPreferences(PREFS_USER_ACCESS_TOKEN, Context.MODE_PRIVATE);

        if (sp.contains(PREF_ACCESS_TOKEN)) {
            AccessToken token = new UserAccessToken();
            token.setToken(sp.getString(PREF_ACCESS_TOKEN, null));
            token.setTokenType(sp.getString(PREF_TOKEN_TYPE, null));
            token.setExpiration(sp.getLong(PREF_EXPIRATION, 0));
            token.setScope(sp.getString(PREF_SCOPE, null));
            token.setRefreshToken(sp.getString(PREF_REFRESH_TOKEN, null));
            return token;
        }

        return null;
    }

    public AccessToken getSavedApplicationAccessToken() {
        SharedPreferences sp =  mContext.getSharedPreferences(PREFS_APPLICATION_ACCESS_TOKEN, Context.MODE_PRIVATE);

        if (sp.contains(PREF_ACCESS_TOKEN)) {
            AccessToken token = new ApplicationAccessToken();
            token.setToken(sp.getString(PREF_ACCESS_TOKEN, null));
            token.setTokenType(sp.getString(PREF_TOKEN_TYPE, null));
            token.setExpiration(sp.getLong(PREF_EXPIRATION, 0));
            token.setScope(sp.getString(PREF_SCOPE, null));
            token.setRefreshToken(sp.getString(PREF_REFRESH_TOKEN, null));
            return token;
        }

        return null;
    }

    public void saveUserAccessTokenResponse(AuthorizationResponse response) {
        mUserAccessToken = new UserAccessToken();
        mUserAccessToken.setToken(response.getToken());
        mUserAccessToken.setTokenType(response.getTokenType());
        mUserAccessToken.setExpiration(response.getExpiresIn()*1000 + new Date().getTime());
        mUserAccessToken.setScope(response.getScope());
        mUserAccessToken.setRefreshToken(response.getRefreshToken());
        saveUserAccessToken();
    }

    public void saveApplicationAccessTokenResponse(AuthorizationResponse response) {
        mApplicationAccessToken = new ApplicationAccessToken();
        mApplicationAccessToken.setToken(response.getToken());
        mApplicationAccessToken.setTokenType(response.getTokenType());
        mApplicationAccessToken.setExpiration(response.getExpiresIn()*1000 + new Date().getTime());
        mApplicationAccessToken.setScope(response.getScope());
        mApplicationAccessToken.setRefreshToken(response.getRefreshToken());
        saveApplicationAccessToken();
    }

    private void saveUserAccessToken() {
        Log.d(TAG, "--ACCESS TOKEN RESPONSE--");
        Log.d(TAG, "Access Token: " + mUserAccessToken.getToken());
        Log.d(TAG, "Refresh Token: " + mUserAccessToken.getRefreshToken());

        SharedPreferences sp = mContext.getSharedPreferences(PREFS_USER_ACCESS_TOKEN, Context.MODE_PRIVATE);
        sp.edit()
                .putString(PREF_ACCESS_TOKEN, mUserAccessToken.getToken())
                .putString(PREF_TOKEN_TYPE, mUserAccessToken.getTokenType())
                .putLong(PREF_EXPIRATION, mUserAccessToken.getExpiration())
                .putString(PREF_SCOPE, mUserAccessToken.getScope())
                .putString(PREF_REFRESH_TOKEN, mUserAccessToken.getRefreshToken())
                .apply();
    }

    private void saveApplicationAccessToken() {
        Log.d(TAG, "--ACCESS TOKEN RESPONSE--");
        Log.d(TAG, "Access Token: " + mApplicationAccessToken.getToken());
        Log.d(TAG, "Refresh Token: " + mApplicationAccessToken.getRefreshToken());

        SharedPreferences sp = mContext.getSharedPreferences(PREFS_APPLICATION_ACCESS_TOKEN, Context.MODE_PRIVATE);
        sp.edit()
                .putString(PREF_ACCESS_TOKEN, mApplicationAccessToken.getToken())
                .putString(PREF_TOKEN_TYPE, mApplicationAccessToken.getTokenType())
                .putLong(PREF_EXPIRATION, mApplicationAccessToken.getExpiration())
                .putString(PREF_SCOPE, mApplicationAccessToken.getScope())
                .putString(PREF_REFRESH_TOKEN, mApplicationAccessToken.getRefreshToken())
                .apply();
    }

    public void clearSavedUserAccessToken() {
        mUserAccessToken = null;
        mContext.getSharedPreferences(PREFS_USER_ACCESS_TOKEN, Context.MODE_PRIVATE)
                .edit().clear().apply();
    }

    public void clearSavedApplicationAccessToken() {
        mApplicationAccessToken = null;
        mContext.getSharedPreferences(PREFS_APPLICATION_ACCESS_TOKEN, Context.MODE_PRIVATE)
                .edit().clear().apply();
    }

    public UserIdentity getUserIdentity() {
        if (mUserIdentity == null) {
            mUserIdentity = getSavedUserIdentity();
        }
        return mUserIdentity;
    }

    public UserIdentity getSavedUserIdentity() {
        SharedPreferences prefs = mContext.getSharedPreferences(PREFS_USER_IDENTITY, Context.MODE_PRIVATE);

        if (prefs.contains(PREF_ID)) {
            UserIdentity id = new UserIdentity();
            id.hasMail(prefs.getBoolean(PREF_HAS_MAIL, false));
            id.setName(prefs.getString(PREF_NAME, null));
            id.setCreated(prefs.getLong(PREF_CREATED, new Date().getTime()));
            id.isHiddenFromRobots(prefs.getBoolean(PREF_HIDE_FROM_ROBOTS, false));
            id.setGoldCreddits(prefs.getInt(PREF_GOLD_CREDDITS, 0));
            id.setCreatedUTC(prefs.getLong(PREF_CREATED_UTC, new Date().getTime()));
            id.hasModMail(prefs.getBoolean(PREF_HAS_MOD_MAIL, false));
            id.setLinkKarma(prefs.getInt(PREF_LINK_KARMA, 0));
            id.setCommentKarma(prefs.getInt(PREF_COMMENT_KARMA, 0));
            id.isOver18(prefs.getBoolean(PREF_IS_OVER_18, false));
            id.isGold(prefs.getBoolean(PREF_IS_GOLD, false));
            id.isMod(prefs.getBoolean(PREF_IS_MOD, false));
            id.setGoldExpiration(prefs.getLong(PREF_GOLD_EXPIRATION, 0));
            id.hasVerifiedEmail(prefs.getBoolean(PREF_HAS_VERIFIED_EMAIL, false));
            id.setId(prefs.getString(PREF_ID, null));
            id.setInboxCount(prefs.getInt(PREF_INBOX_COUNT, 0));
            return id;
        }

        return null;
    }

    public void saveUserIdentity(UserIdentity identity) {
        Boolean hasMail = identity.hasMail();
        String name = identity.getName();
        Long created = identity.getCreated();
        Boolean isHiddenFromRobots = identity.isHiddenFromRobots();
        Integer goldCreddits = identity.getGoldCreddits();
        Long createdUTC = identity.getCreatedUTC();
        Boolean hasModMail = identity.hasModMail();
        Integer linkKarma = identity.getLinkKarma();
        Integer commentKarma = identity.getCommentKarma();
        Boolean isOver18 = identity.isOver18();
        Boolean isGold = identity.isGold();
        Boolean isMod = identity.isMod();
        Long goldExpiration = identity.getGoldExpiration();
        Boolean hasVerifiedEmail = identity.hasVerifiedEmail();
        String id = identity.getId();
        Integer inboxCount = identity.getInboxCount();

        SharedPreferences prefs = mContext.getSharedPreferences(PREFS_USER_IDENTITY, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(PREF_HAS_MAIL, hasMail)
                .putString(PREF_NAME, name)
                .putLong(PREF_CREATED, created)
                .putBoolean(PREF_HIDE_FROM_ROBOTS, isHiddenFromRobots)
                .putInt(PREF_GOLD_CREDDITS, goldCreddits)
                .putLong(PREF_CREATED_UTC, createdUTC)
                .putBoolean(PREF_HAS_MOD_MAIL, hasModMail)
                .putInt(PREF_LINK_KARMA, linkKarma)
                .putInt(PREF_COMMENT_KARMA, commentKarma)
                .putBoolean(PREF_IS_OVER_18, isOver18)
                .putBoolean(PREF_IS_GOLD, isGold)
                .putBoolean(PREF_IS_MOD, isMod)
                .putLong(PREF_GOLD_EXPIRATION, goldExpiration != null ? goldExpiration : 0)
                .putBoolean(PREF_HAS_VERIFIED_EMAIL, hasVerifiedEmail)
                .putString(PREF_ID, id)
                .putInt(PREF_INBOX_COUNT, inboxCount)
                .apply();

        mBus.post(new UserIdentitySavedEvent(identity));
    }

    public void clearSavedUserIdentity() {
        mUserIdentity = null;
        mContext.getSharedPreferences(PREFS_USER_IDENTITY, Context.MODE_PRIVATE)
                .edit().clear().apply();
//        mBus.post(new UserIdentitySavedEvent(null));
    }

    public static RedditIdentityManager getInstance(Context context) {
        if (_instance == null) {
            synchronized (RedditIdentityManager.class) {
                if (_instance == null) {
                    _instance = new RedditIdentityManager(context);
                }
            }
        }
        return _instance;
    }
}
