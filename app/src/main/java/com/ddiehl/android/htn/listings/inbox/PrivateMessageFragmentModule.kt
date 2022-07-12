package com.ddiehl.android.htn.listings.inbox

import androidx.fragment.app.FragmentActivity
import dagger.Module
import dagger.Provides

@Module
class PrivateMessageFragmentModule {

    @Provides
    fun bindFragmentActivity(fragment: PrivateMessageFragment): FragmentActivity = fragment.requireActivity()
}
