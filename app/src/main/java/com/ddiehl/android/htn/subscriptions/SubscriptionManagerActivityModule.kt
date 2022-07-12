package com.ddiehl.android.htn.subscriptions

import androidx.fragment.app.FragmentActivity
import com.ddiehl.android.htn.di.ActivityScope
import dagger.Module
import dagger.Provides

@Module
class SubscriptionManagerActivityModule {

    @Provides
    @ActivityScope
    fun provideFragmentActivity(activity: SubscriptionManagerActivity): FragmentActivity = activity
}
