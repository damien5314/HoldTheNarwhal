package com.ddiehl.android.htn.listings.subreddit

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.ddiehl.android.htn.view.MainView
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
interface SubredditFragmentModule {

    companion object {

        @Provides
        fun bindFragmentActivity(fragment: SubredditFragment): FragmentActivity = fragment.requireActivity()
    }

    @Binds
    fun bindFragment(fragment: SubredditFragment): Fragment

    @Binds
    fun bindMainView(fragment: SubredditFragment): MainView

    @Binds
    fun bindSubredditView(fragment: SubredditFragment): SubredditView
}
