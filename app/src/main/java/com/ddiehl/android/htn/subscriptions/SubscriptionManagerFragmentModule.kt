package com.ddiehl.android.htn.subscriptions

import androidx.fragment.app.FragmentActivity
import dagger.Module
import dagger.Provides

@Module
class SubscriptionManagerFragmentModule {

    @Provides
    fun bindFragmentActivity(fragment: SubscriptionManagerFragment): FragmentActivity = fragment.requireActivity()
}
