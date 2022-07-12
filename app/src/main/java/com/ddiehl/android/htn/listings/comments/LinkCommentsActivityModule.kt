package com.ddiehl.android.htn.listings.comments

import androidx.fragment.app.FragmentActivity
import com.ddiehl.android.htn.di.ActivityScope
import dagger.Module
import dagger.Provides

@Module
class LinkCommentsActivityModule {

    @Provides
    @ActivityScope
    fun provideFragmentActivity(activity: LinkCommentsActivity): FragmentActivity = activity
}
