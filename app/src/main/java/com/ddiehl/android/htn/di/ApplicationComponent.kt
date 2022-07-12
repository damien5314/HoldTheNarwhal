package com.ddiehl.android.htn.di

import com.ddiehl.android.htn.HoldTheNarwhal
import com.ddiehl.android.htn.listings.BaseListingsPresenter
import com.ddiehl.android.htn.listings.comments.ThreadStubViewHolder
import com.ddiehl.android.htn.listings.inbox.PrivateMessageAdapter
import com.ddiehl.android.htn.listings.profile.UserProfilePresenter
import com.ddiehl.android.htn.listings.subreddit.submission.SubmitPostPresenter
import com.ddiehl.android.htn.settings.SettingsPresenter
import com.ddiehl.android.htn.subscriptions.SubscriptionManagerPresenter
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        ApplicationModule::class,
        ActivitiesBindingModule::class,
        FragmentsBindingModule::class,
        ServicesBindingModule::class,
    ]
)
interface ApplicationComponent {

    fun inject(application: HoldTheNarwhal)

    // Below injectors are deprecated.
    // All new additions should go into the *BindingModules listed as part of this component.
    // TODO: Refactor out the remainder of these

    // ViewHolders
    fun inject(vh: ThreadStubViewHolder)
    fun inject(vh: PrivateMessageAdapter.VH)

    // Presenters
    fun inject(presenter: BaseListingsPresenter)
    fun inject(presenter: UserProfilePresenter)
    fun inject(presenter: SettingsPresenter)
    fun inject(presenter: SubscriptionManagerPresenter)
    fun inject(presenter: SubmitPostPresenter)
}
