package com.ddiehl.android.htn.di

import com.ddiehl.android.htn.notifications.UnreadInboxCheckJobService
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Provides subcomponent bindings for all Service classes using Dagger's [ContributesAndroidInjector].
 */
@Module
interface ServicesBindingModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    fun bindUnreadInboxCheckJobService(): UnreadInboxCheckJobService
}
