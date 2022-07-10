package com.ddiehl.android.htn.di

import com.ddiehl.android.htn.listings.inbox.PrivateMessageActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * TODO: Describe what this class is responsible for
 */
@Module
interface ActivitiesBindingModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    fun bindPrivateMessageActivity(): PrivateMessageActivity
}
