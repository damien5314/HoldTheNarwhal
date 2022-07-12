package com.ddiehl.android.htn.listings.subreddit

import androidx.fragment.app.FragmentActivity
import com.ddiehl.android.htn.di.ActivityScope
import dagger.Module
import dagger.Provides

@Module
class SubredditActivityModule {

    @Provides
    @ActivityScope
    fun provideFragmentActivity(activity: SubredditActivity): FragmentActivity = activity
}
