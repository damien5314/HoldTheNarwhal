package com.ddiehl.android.simpleredditreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.ddiehl.android.simpleredditreader.events.UserAuthorizedEvent;
import com.ddiehl.android.simpleredditreader.events.UserIdentityRetrievedEvent;
import com.ddiehl.android.simpleredditreader.model.UserIdentity;
import com.squareup.otto.Subscribe;

import java.util.Date;

public class UserIdentityAccessProxy {
    private static final String TAG = UserIdentityAccessProxy.class.getSimpleName();

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

    private static UserIdentityAccessProxy _instance;

    private Context mContext;
    private UserIdentity mUserIdentity;

    private UserIdentityAccessProxy(Context context) {
        mContext = context.getApplicationContext();
        getUserIdentity();
    }

    public static UserIdentityAccessProxy getInstance(Context context) {
        if (_instance == null) {
            synchronized (UserIdentityAccessProxy.class) {
                if (_instance == null) {
                    _instance = new UserIdentityAccessProxy(context);
                }
            }
        }
        return _instance;
    }

    public UserIdentity getUserIdentity() {
        if (mUserIdentity == null) {
            mUserIdentity = new UserIdentity();

            SharedPreferences prefs = mContext.getSharedPreferences(PREFS_USER_IDENTITY, Context.MODE_PRIVATE);
            mUserIdentity.hasMail(prefs.getBoolean(PREF_HAS_MAIL, false));
            mUserIdentity.setName(prefs.getString(PREF_NAME, null));
            mUserIdentity.setCreated(prefs.getLong(PREF_CREATED, new Date().getTime()));
            mUserIdentity.isHiddenFromRobots(prefs.getBoolean(PREF_HIDE_FROM_ROBOTS, false));
            mUserIdentity.setGoldCreddits(prefs.getInt(PREF_GOLD_CREDDITS, 0));
            mUserIdentity.setCreatedUTC(prefs.getLong(PREF_CREATED_UTC, new Date().getTime()));
            mUserIdentity.hasModMail(prefs.getBoolean(PREF_HAS_MOD_MAIL, false));
            mUserIdentity.setLinkKarma(prefs.getInt(PREF_LINK_KARMA, 0));
            mUserIdentity.setCommentKarma(prefs.getInt(PREF_COMMENT_KARMA, 0));
            mUserIdentity.isOver18(prefs.getBoolean(PREF_IS_OVER_18, false));
            mUserIdentity.isGold(prefs.getBoolean(PREF_IS_GOLD, false));
            mUserIdentity.isMod(prefs.getBoolean(PREF_IS_MOD, false));
            mUserIdentity.setGoldExpiration(prefs.getLong(PREF_GOLD_EXPIRATION, 0));
            mUserIdentity.hasVerifiedEmail(prefs.getBoolean(PREF_HAS_VERIFIED_EMAIL, false));
            mUserIdentity.setId(prefs.getString(PREF_ID, null));
            mUserIdentity.setInboxCount(prefs.getInt(PREF_INBOX_COUNT, 0));
        }
        return mUserIdentity;
    }

    public void clearUserIdentity() {
        SharedPreferences prefs = mContext.getSharedPreferences(PREFS_USER_IDENTITY, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    @Subscribe
    public void onUserAuthorized(UserAuthorizedEvent event) {
        if (!event.isFailed()) {
            clearUserIdentity();
        }
    }

    @Subscribe
    public void onUserIdentityRetrieved(UserIdentityRetrievedEvent event) {
        if (event.isFailed()) {
            Log.d(TAG, "Error retrieving user identity");
            return;
        }

        mUserIdentity = event.getUserIdentity();
        Toast.makeText(mContext, "Retrieved identity for user: " + mUserIdentity.getName(), Toast.LENGTH_LONG).show();

        Boolean hasMail = mUserIdentity.hasMail();
        String name = mUserIdentity.getName();
        Long created = mUserIdentity.getCreated();
        Boolean isHiddenFromRobots = mUserIdentity.isHiddenFromRobots();
        Integer goldCreddits = mUserIdentity.getGoldCreddits();
        Long createdUTC = mUserIdentity.getCreatedUTC();
        Boolean hasModMail = mUserIdentity.hasModMail();
        Integer linkKarma = mUserIdentity.getLinkKarma();
        Integer commentKarma = mUserIdentity.getCommentKarma();
        Boolean isOver18 = mUserIdentity.isOver18();
        Boolean isGold = mUserIdentity.isGold();
        Boolean isMod = mUserIdentity.isMod();
        Long goldExpiration = mUserIdentity.getGoldExpiration();
        Boolean hasVerifiedEmail = mUserIdentity.hasVerifiedEmail();
        String id = mUserIdentity.getId();
        Integer inboxCount = mUserIdentity.getInboxCount();

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
    }
}
