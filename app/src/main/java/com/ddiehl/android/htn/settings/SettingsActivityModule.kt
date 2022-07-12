package com.ddiehl.android.htn.settings

import androidx.fragment.app.FragmentActivity
import com.ddiehl.android.htn.di.ActivityScope
import dagger.Module
import dagger.Provides

@Module
class SettingsActivityModule {

    @Provides
    @ActivityScope
    fun provideFragmentActivity(activity: SettingsActivity): FragmentActivity = activity
}
