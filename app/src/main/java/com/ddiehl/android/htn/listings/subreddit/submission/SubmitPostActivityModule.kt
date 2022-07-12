package com.ddiehl.android.htn.listings.subreddit.submission

import androidx.fragment.app.FragmentActivity
import com.ddiehl.android.htn.di.ActivityScope
import dagger.Module
import dagger.Provides

@Module
class SubmitPostActivityModule {

    @Provides
    @ActivityScope
    fun provideFragmentActivity(activity: SubmitPostActivity): FragmentActivity = activity
}
