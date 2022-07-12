package com.ddiehl.android.htn.navigation

import androidx.fragment.app.FragmentActivity
import com.ddiehl.android.htn.di.ActivityScope
import dagger.Module
import dagger.Provides

@Module
class DeepLinkDispatcherModule {

    @Provides
    @ActivityScope
    fun provideFragmentActivity(activity: DeepLinkDispatcher): FragmentActivity = activity
}
