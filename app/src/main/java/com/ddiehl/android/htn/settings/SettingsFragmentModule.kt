package com.ddiehl.android.htn.settings

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class SettingsFragmentModule {

    companion object {
        const val USER_PREFERENCES = "userPreferences"
    }

    @Provides
    fun providesSettingsView(fragment: SettingsFragment): SettingsView = fragment

    @Provides
    @Named(USER_PREFERENCES)
    fun providesUserSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(SettingsManagerImpl.PREFS_USER, Context.MODE_PRIVATE)
    }
}
