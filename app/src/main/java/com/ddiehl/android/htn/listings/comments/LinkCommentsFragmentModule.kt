package com.ddiehl.android.htn.listings.comments

import androidx.fragment.app.FragmentActivity
import dagger.Module
import dagger.Provides

@Module
class LinkCommentsFragmentModule {

    @Provides
    fun bindFragmentActivity(fragment: LinkCommentsFragment): FragmentActivity = fragment.requireActivity()
}
