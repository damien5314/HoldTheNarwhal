package com.ddiehl.android.htn.di

import android.content.Context
import com.ddiehl.android.htn.BuildConfig
import com.ddiehl.android.htn.identity.IdentityManager
import com.ddiehl.android.htn.identity.IdentityManagerImpl
import com.ddiehl.android.htn.managers.NetworkConnectivityManager
import com.ddiehl.android.htn.settings.SettingsManager
import com.ddiehl.android.htn.settings.SettingsManagerImpl
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import rxreddit.android.AndroidAccessTokenManager
import rxreddit.android.AndroidUtil
import rxreddit.api.RedditService
import rxreddit.util.RxRedditUtil
import java.io.File
import javax.inject.Named
import javax.inject.Singleton

@Module
class ApplicationModule(context: Context) {

    private val appContext: Context = context.applicationContext

    @Provides
    fun providesAppContext(): Context {
        return appContext
    }

    @Singleton
    @Provides
    fun providesIdentityManager(context: Context?, settingsManager: SettingsManager?): IdentityManager {
        return IdentityManagerImpl(context, settingsManager)
    }

    @Singleton
    @Provides
    fun providesSettingsManager(context: Context?): SettingsManager {
        return SettingsManagerImpl(context!!)
    }

    @Provides
    @Named("RedditServiceScope")
    fun providesRedditServiceScope(): List<String> {
        return listOf(
            "identity",
            "mysubreddits",
            "privatemessages",
            "read",
            "report",
            "save",
            "submit",
            "vote",
            "history",
            "account",
            "subscribe",
        )
    }

    @Singleton
    @Provides
    fun providesRedditService(
        context: Context,
        @Named("RedditServiceScope") scopeList: List<String>,
    ): RedditService {
//        return new RedditServiceMock();
        val cacheSize = 10 * 1024 * 1024 // 10 MiB
        val path = File(context.cacheDir, "htn-http-cache")
        val userAgent = RxRedditUtil.getUserAgent(
            "android", "com.ddiehl.android.htn", BuildConfig.VERSION_NAME, "damien5314"
        )
        return RedditService.Builder()
            .appId(BuildConfig.REDDIT_APP_ID)
            .redirectUri(BuildConfig.REDDIT_REDIRECT_URI)
            .scopes(scopeList)
            .deviceId(AndroidUtil.getDeviceId(context))
            .userAgent(userAgent)
            .accessTokenManager(AndroidAccessTokenManager(context))
            .cache(cacheSize, path)
            .loggingEnabled(BuildConfig.DEBUG)
            .build()
    }

    @Provides
    fun providesGson(redditService: RedditService): Gson {
        return redditService.gson
    }

    @Provides
    fun providesNetworkConnectivityManager(context: Context): NetworkConnectivityManager {
        return NetworkConnectivityManager(context)
    }
}
