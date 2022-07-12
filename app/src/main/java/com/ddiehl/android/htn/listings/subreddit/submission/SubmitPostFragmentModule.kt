package com.ddiehl.android.htn.listings.subreddit.submission

import androidx.fragment.app.FragmentActivity
import dagger.Module
import dagger.Provides

@Module
class SubmitPostFragmentModule {

    @Provides
    fun bindFragmentActivity(fragment: SubmitPostFragment): FragmentActivity = fragment.requireActivity()
}
