package com.ddiehl.android.htn.di

import com.ddiehl.android.htn.listings.comments.LinkCommentsActivity
import com.ddiehl.android.htn.listings.inbox.InboxActivity
import com.ddiehl.android.htn.listings.inbox.PrivateMessageActivity
import com.ddiehl.android.htn.listings.profile.UserProfileActivity
import com.ddiehl.android.htn.listings.subreddit.SubredditActivity
import com.ddiehl.android.htn.listings.subreddit.submission.SubmitPostActivity
import com.ddiehl.android.htn.navigation.WebViewActivity
import com.ddiehl.android.htn.settings.SettingsActivity
import com.ddiehl.android.htn.subredditinfo.SubredditInfoActivity
import com.ddiehl.android.htn.subscriptions.SubscriptionManagerActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Provides subcomponent bindings for all Activity classes using Dagger's [ContributesAndroidInjector].
 */
@Module
interface ActivitiesBindingModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    fun bindInboxActivity(): InboxActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    fun bindLinkCommentsActivity(): LinkCommentsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    fun bindPrivateMessageActivity(): PrivateMessageActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    fun bindSettingsActivity(): SettingsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    fun bindSubmitPostActivity(): SubmitPostActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    fun bindSubredditActivity(): SubredditActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    fun bindSubredditInfoActivity(): SubredditInfoActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    fun bindSubscriptionManagerActivity(): SubscriptionManagerActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    fun bindUserProfileActivity(): UserProfileActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    fun bindWebViewActivity(): WebViewActivity
}
