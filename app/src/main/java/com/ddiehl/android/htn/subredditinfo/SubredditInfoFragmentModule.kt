package com.ddiehl.android.htn.subredditinfo

import androidx.fragment.app.FragmentActivity
import dagger.Module
import dagger.Provides

@Module
class SubredditInfoFragmentModule {

    @Provides
    fun bindFragmentActivity(fragment: SubredditInfoFragment): FragmentActivity = fragment.requireActivity()
}
