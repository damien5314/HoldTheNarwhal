package com.ddiehl.android.htn.di

import com.ddiehl.android.htn.gallery.MediaGalleryFragment
import com.ddiehl.android.htn.listings.comments.LinkCommentsFragment
import com.ddiehl.android.htn.listings.inbox.InboxFragment
import com.ddiehl.android.htn.listings.inbox.PrivateMessageFragment
import com.ddiehl.android.htn.listings.profile.UserProfileFragment
import com.ddiehl.android.htn.listings.report.ReportView
import com.ddiehl.android.htn.listings.subreddit.SubredditFragment
import com.ddiehl.android.htn.listings.subreddit.submission.SubmitPostFragment
import com.ddiehl.android.htn.navigation.WebViewFragment
import com.ddiehl.android.htn.subredditinfo.SubredditInfoFragment
import com.ddiehl.android.htn.subscriptions.SubscriptionManagerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * TODO: Describe what this class is responsible for
 */
@Module
interface FragmentsBindingModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [])
    fun bindInboxFragment(): InboxFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [])
    fun bindLinkCommentsFragment(): LinkCommentsFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [])
    fun bindMediaGalleryFragment(): MediaGalleryFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [])
    fun bindPrivateMessageFragment(): PrivateMessageFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [])
    fun bindReportView(): ReportView

    @FragmentScope
    @ContributesAndroidInjector(modules = [])
    fun bindSubmitPostFragment(): SubmitPostFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [])
    fun bindSubredditFragment(): SubredditFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [])
    fun bindSubredditInfoFragment(): SubredditInfoFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [])
    fun bindSubscriptionManagerFragment(): SubscriptionManagerFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [])
    fun bindUserProfileFragment(): UserProfileFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [])
    fun bindWebViewFragment(): WebViewFragment
}
