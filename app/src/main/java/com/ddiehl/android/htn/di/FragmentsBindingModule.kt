package com.ddiehl.android.htn.di

import com.ddiehl.android.htn.gallery.MediaGalleryFragment
import com.ddiehl.android.htn.listings.comments.AddCommentDialog
import com.ddiehl.android.htn.listings.comments.ChooseCommentSortDialog
import com.ddiehl.android.htn.listings.comments.LinkCommentsFragment
import com.ddiehl.android.htn.listings.inbox.InboxFragment
import com.ddiehl.android.htn.listings.inbox.PrivateMessageFragment
import com.ddiehl.android.htn.listings.links.ChooseLinkSortDialog
import com.ddiehl.android.htn.listings.profile.UserProfileFragment
import com.ddiehl.android.htn.listings.report.ReportDialog
import com.ddiehl.android.htn.listings.report.ReportView
import com.ddiehl.android.htn.listings.subreddit.NsfwWarningDialog
import com.ddiehl.android.htn.listings.subreddit.SubredditFragment
import com.ddiehl.android.htn.listings.subreddit.submission.SubmitPostFragment
import com.ddiehl.android.htn.navigation.ConfirmExitDialog
import com.ddiehl.android.htn.navigation.ConfirmSignOutDialog
import com.ddiehl.android.htn.navigation.WebViewFragment
import com.ddiehl.android.htn.settings.SettingsFragment
import com.ddiehl.android.htn.settings.SettingsFragmentModule
import com.ddiehl.android.htn.subredditinfo.SubredditInfoFragment
import com.ddiehl.android.htn.subscriptions.SubredditSearchDialog
import com.ddiehl.android.htn.subscriptions.SubscriptionManagerFragment
import com.ddiehl.android.htn.view.video.VideoPlayerDialog
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Provides subcomponent bindings for all Fragment classes using Dagger's [ContributesAndroidInjector].
 */
@Module
interface FragmentsBindingModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [])
    fun bindAddCommentDialog(): AddCommentDialog

    @FragmentScope
    @ContributesAndroidInjector(modules = [])
    fun bindChooseCommentSortDialog(): ChooseCommentSortDialog

    @FragmentScope
    @ContributesAndroidInjector(modules = [])
    fun bindChooseLinkSortDialog(): ChooseLinkSortDialog

    @FragmentScope
    @ContributesAndroidInjector(modules = [])
    fun bindConfirmExitDialog(): ConfirmExitDialog

    @FragmentScope
    @ContributesAndroidInjector(modules = [])
    fun bindConfirmSignOutDialog(): ConfirmSignOutDialog

    @FragmentScope
    @ContributesAndroidInjector(modules = [])
    fun bindNsfwWarningDialog(): NsfwWarningDialog

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
    fun bindReportDialog(): ReportDialog

    @FragmentScope
    @ContributesAndroidInjector(modules = [])
    fun bindReportView(): ReportView

    @FragmentScope
    @ContributesAndroidInjector(modules = [
        SettingsFragmentModule::class,
    ])
    fun bindSettingsFragment(): SettingsFragment

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
    fun bindSubredditSearchDialog(): SubredditSearchDialog

    @FragmentScope
    @ContributesAndroidInjector(modules = [])
    fun bindSubscriptionManagerFragment(): SubscriptionManagerFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [])
    fun bindUserProfileFragment(): UserProfileFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [])
    fun bindWebViewFragment(): WebViewFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [])
    fun bindVideoPlayerDialog(): VideoPlayerDialog
}
