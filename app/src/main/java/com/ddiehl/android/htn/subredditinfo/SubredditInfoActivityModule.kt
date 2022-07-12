package com.ddiehl.android.htn.subredditinfo

import androidx.fragment.app.FragmentActivity
import com.ddiehl.android.htn.di.ActivityScope
import dagger.Module
import dagger.Provides

@Module
class SubredditInfoActivityModule {

    @Provides
    @ActivityScope
    fun provideFragmentActivity(activity: SubredditInfoActivity): FragmentActivity = activity
}
