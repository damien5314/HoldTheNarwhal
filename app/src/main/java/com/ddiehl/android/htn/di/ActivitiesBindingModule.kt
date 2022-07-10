package com.ddiehl.android.htn.di

import com.ddiehl.android.htn.listings.inbox.PrivateMessageActivity
import com.ddiehl.android.htn.listings.subreddit.SubredditActivity
import com.ddiehl.android.htn.settings.SettingsActivity
import com.ddiehl.android.htn.subscriptions.SubscriptionManagerActivity
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

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    fun bindSettingsActivity(): SettingsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    fun bindSubredditActivity(): SubredditActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    fun bindSubscriptionManagerActivity(): SubscriptionManagerActivity
}
