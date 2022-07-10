package com.ddiehl.android.htn.di

import com.ddiehl.android.htn.listings.inbox.PrivateMessageActivity
import com.ddiehl.android.htn.listings.subreddit.SubredditActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * TODO: Describe what this class is responsible for
 */
@Module
interface ActivitiesBindingModule {

    // I don't think we need this as long as every BaseActivity subclass has an injector
//    @ActivityScope
//    @ContributesAndroidInjector(modules = [])
//    fun bindBaseActivity(): BaseActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    fun bindPrivateMessageActivity(): PrivateMessageActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    fun bindSubredditActivity(): SubredditActivity
}
