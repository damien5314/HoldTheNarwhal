package com.ddiehl.android.htn;

import android.content.Context;
import android.content.SharedPreferences;

import com.ddiehl.android.htn.settings.SettingsManager;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import rxreddit.model.UserIdentity;

public class IdentityManagerImpl implements IdentityManager {

    private static final String PREFS_USER_IDENTITY = "prefs_user_identity";
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

    private final Context mContext;
    private final SettingsManager mSettingsManager;
    private UserIdentity mUserIdentity;
    private final Set<Callbacks> mListeners = new HashSet<>();

    public IdentityManagerImpl(Context context, SettingsManager settingsManager) {
        mContext = context.getApplicationContext();
        mSettingsManager = settingsManager;
        mUserIdentity = getSavedUserIdentity();
    }

    @Override
    public UserIdentity getUserIdentity() {
        if (mUserIdentity == null) {
            mUserIdentity = getSavedUserIdentity();
        }
        return mUserIdentity;
    }

    private UserIdentity getSavedUserIdentity() {
        SharedPreferences prefs = mContext.getSharedPreferences(PREFS_USER_IDENTITY, Context.MODE_PRIVATE);
        if (!prefs.contains(PREF_ID)) return null;
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

    @Override
    public void saveUserIdentity(UserIdentity identity) {
        mUserIdentity = identity;
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
        SharedPreferences prefs =
                mContext.getSharedPreferences(PREFS_USER_IDENTITY, Context.MODE_PRIVATE);
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
        notifyListeners();
    }

    @Override
    public void clearSavedUserIdentity() {
        mUserIdentity = null;
        mContext.getSharedPreferences(PREFS_USER_IDENTITY, Context.MODE_PRIVATE)
                .edit().clear().apply();
        mSettingsManager.clearUserSettings();
        notifyListeners();
    }

    @Override
    public void registerUserIdentityChangeListener(Callbacks listener) {
        mListeners.add(listener);
    }

    @Override
    public void unregisterUserIdentityChangeListener(Callbacks listener) {
        mListeners.remove(listener);
    }

    private void notifyListeners() {
        for (Callbacks listener : mListeners) {
            listener.onUserIdentityChanged().call(mUserIdentity);
        }
    }
}
