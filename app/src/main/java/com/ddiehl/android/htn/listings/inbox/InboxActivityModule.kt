package com.ddiehl.android.htn.listings.inbox

import androidx.fragment.app.FragmentActivity
import com.ddiehl.android.htn.di.ActivityScope
import dagger.Module
import dagger.Provides

@Module
class InboxActivityModule {

    @Provides
    @ActivityScope
    fun provideFragmentActivity(activity: InboxActivity): FragmentActivity = activity
}
