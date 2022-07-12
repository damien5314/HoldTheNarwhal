package com.ddiehl.android.htn.di

import com.ddiehl.android.htn.listings.comments.LinkCommentsActivity
import com.ddiehl.android.htn.listings.comments.LinkCommentsActivityModule
import com.ddiehl.android.htn.listings.inbox.InboxActivity
import com.ddiehl.android.htn.listings.inbox.InboxActivityModule
import com.ddiehl.android.htn.listings.inbox.PrivateMessageActivity
import com.ddiehl.android.htn.listings.inbox.PrivateMessageActivityModule
import com.ddiehl.android.htn.listings.profile.UserProfileActivity
import com.ddiehl.android.htn.listings.profile.UserProfileActivityModule
import com.ddiehl.android.htn.listings.subreddit.SubredditActivity
import com.ddiehl.android.htn.listings.subreddit.SubredditActivityModule
import com.ddiehl.android.htn.listings.subreddit.submission.SubmitPostActivity
import com.ddiehl.android.htn.listings.subreddit.submission.SubmitPostActivityModule
import com.ddiehl.android.htn.navigation.DeepLinkDispatcher
import com.ddiehl.android.htn.navigation.DeepLinkDispatcherModule
import com.ddiehl.android.htn.navigation.WebViewActivity
import com.ddiehl.android.htn.navigation.WebViewActivityModule
import com.ddiehl.android.htn.settings.SettingsActivity
import com.ddiehl.android.htn.settings.SettingsActivityModule
import com.ddiehl.android.htn.subredditinfo.SubredditInfoActivity
import com.ddiehl.android.htn.subredditinfo.SubredditInfoActivityModule
import com.ddiehl.android.htn.subscriptions.SubscriptionManagerActivity
import com.ddiehl.android.htn.subscriptions.SubscriptionManagerActivityModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Provides subcomponent bindings for all Activity classes using Dagger's [ContributesAndroidInjector].
 */
@Module
interface ActivitiesBindingModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [
        DeepLinkDispatcherModule::class,
    ])
    fun bindDeepLinkDispatcher(): DeepLinkDispatcher

    @ActivityScope
    @ContributesAndroidInjector(modules = [
        InboxActivityModule::class,
    ])
    fun bindInboxActivity(): InboxActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [
        LinkCommentsActivityModule::class,
    ])
    fun bindLinkCommentsActivity(): LinkCommentsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [
        PrivateMessageActivityModule::class,
    ])
    fun bindPrivateMessageActivity(): PrivateMessageActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [
        SettingsActivityModule::class,
    ])
    fun bindSettingsActivity(): SettingsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [
        SubmitPostActivityModule::class,
    ])
    fun bindSubmitPostActivity(): SubmitPostActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [
        SubredditActivityModule::class,
    ])
    fun bindSubredditActivity(): SubredditActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [
        SubredditInfoActivityModule::class,
    ])
    fun bindSubredditInfoActivity(): SubredditInfoActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [
        SubscriptionManagerActivityModule::class,
    ])
    fun bindSubscriptionManagerActivity(): SubscriptionManagerActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [
        UserProfileActivityModule::class,
    ])
    fun bindUserProfileActivity(): UserProfileActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [
        WebViewActivityModule::class,
    ])
    fun bindWebViewActivity(): WebViewActivity
}
