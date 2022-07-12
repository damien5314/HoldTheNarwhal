package com.ddiehl.android.htn.listings.profile

import androidx.fragment.app.FragmentActivity
import com.ddiehl.android.htn.di.ActivityScope
import dagger.Module
import dagger.Provides

@Module
class UserProfileActivityModule {

    @Provides
    @ActivityScope
    fun provideFragmentActivity(activity: UserProfileActivity): FragmentActivity = activity
}
