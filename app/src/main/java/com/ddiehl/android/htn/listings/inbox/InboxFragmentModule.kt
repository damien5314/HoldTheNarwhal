package com.ddiehl.android.htn.listings.inbox

import androidx.fragment.app.FragmentActivity
import dagger.Module
import dagger.Provides

@Module
class InboxFragmentModule {

    @Provides
    fun bindFragmentActivity(fragment: InboxFragment): FragmentActivity = fragment.requireActivity()
}
