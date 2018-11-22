package com.ddiehl.android.htn.settings

import android.content.Context
import android.content.SharedPreferences
import com.ddiehl.android.htn.managers.NetworkConnectivityManager
import dagger.Module
import dagger.Provides
import rxreddit.api.RedditService
import javax.inject.Named

@Module
class SettingsFragmentModule(private val settingsView: SettingsView) {

    companion object {
        const val USER_PREFERENCES = "userPreferences"
    }

    @Provides
    @Named(USER_PREFERENCES)
    fun providesUserSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(SettingsManagerImpl.PREFS_USER, Context.MODE_PRIVATE)
    }

    @Provides
    fun provideSettingsPresenter(
        @Named(USER_PREFERENCES) userPreferences: SharedPreferences,
        redditService: RedditService,
        settingsManager: SettingsManager,
        networkConnectivityManager: NetworkConnectivityManager
    ): SettingsPresenter {
        return SettingsPresenter(
            settingsView,
            userPreferences,
            redditService,
            settingsManager,
            networkConnectivityManager
        )
    }
}
