package com.ddiehl.android.htn.listings.subreddit

import androidx.fragment.app.FragmentActivity
import dagger.Module
import dagger.Provides

@Module
class SubredditFragmentModule {

    @Provides
    fun bindFragmentActivity(fragment: SubredditFragment): FragmentActivity = fragment.requireActivity()
}