package com.ddiehl.android.htn.di

import com.ddiehl.android.htn.HoldTheNarwhal
import com.ddiehl.android.htn.gallery.MediaGalleryFragment
import com.ddiehl.android.htn.listings.BaseListingsFragment
import com.ddiehl.android.htn.listings.BaseListingsPresenter
import com.ddiehl.android.htn.listings.comments.LinkCommentsFragment
import com.ddiehl.android.htn.listings.comments.ThreadStubViewHolder
import com.ddiehl.android.htn.listings.inbox.InboxFragment
import com.ddiehl.android.htn.listings.inbox.PrivateMessageAdapter
import com.ddiehl.android.htn.listings.inbox.PrivateMessageFragment
import com.ddiehl.android.htn.listings.profile.UserProfileFragment
import com.ddiehl.android.htn.listings.profile.UserProfilePresenter
import com.ddiehl.android.htn.listings.subreddit.SubredditFragment
import com.ddiehl.android.htn.listings.subreddit.submission.SubmitPostFragment
import com.ddiehl.android.htn.listings.subreddit.submission.SubmitPostPresenter
import com.ddiehl.android.htn.navigation.WebViewFragment
import com.ddiehl.android.htn.notifications.UnreadInboxCheckJobService
import com.ddiehl.android.htn.settings.SettingsFragmentComponent
import com.ddiehl.android.htn.settings.SettingsFragmentModule
import com.ddiehl.android.htn.settings.SettingsPresenter
import com.ddiehl.android.htn.subscriptions.SubscriptionManagerPresenter
import com.ddiehl.android.htn.view.BaseFragment
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        ApplicationModule::class,
        ActivitiesBindingModule::class,
        FragmentsBindingModule::class,
    ]
)
interface ApplicationComponent {

    fun inject(application: HoldTheNarwhal)

    // Fragments
    fun inject(fragment: BaseFragment)
    fun inject(fragment: BaseListingsFragment)
    fun inject(fragment: SubredditFragment)
    fun inject(fragment: UserProfileFragment)
    fun inject(fragment: PrivateMessageFragment)
    fun inject(fragment: WebViewFragment)
    fun inject(fragment: LinkCommentsFragment)
    fun inject(fragment: SubmitPostFragment)
    fun inject(fragment: InboxFragment)
    fun inject(mediaGalleryFragment: MediaGalleryFragment)

    // ViewHolders
    fun inject(vh: ThreadStubViewHolder)
    fun inject(vh: PrivateMessageAdapter.VH)

    // Presenters
    fun inject(presenter: BaseListingsPresenter)
    fun inject(presenter: UserProfilePresenter)
    fun inject(presenter: SettingsPresenter)
    fun inject(presenter: SubscriptionManagerPresenter)
    fun inject(presenter: SubmitPostPresenter)

    // Services
    fun inject(service: UnreadInboxCheckJobService)

    // Subcomponents
    operator fun plus(module: SettingsFragmentModule): SettingsFragmentComponent?
}
