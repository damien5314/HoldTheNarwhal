package com.ddiehl.android.htn.listings.inbox

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.ddiehl.android.htn.view.MainView
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
interface InboxFragmentModule {

    companion object {

        @Provides
        fun bindFragmentActivity(fragment: InboxFragment): FragmentActivity = fragment.requireActivity()
    }

    @Binds
    fun bindFragment(fragment: InboxFragment): Fragment

    @Binds
    fun bindMainView(fragment: InboxFragment): MainView

    @Binds
    fun bindInboxView(fragment: InboxFragment): InboxView
}
