package com.ddiehl.android.htn.listings.comments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.ddiehl.android.htn.view.MainView
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
interface LinkCommentsFragmentModule {

    companion object {

        @Provides
        fun bindFragmentActivity(fragment: LinkCommentsFragment): FragmentActivity = fragment.requireActivity()
    }

    @Binds
    fun bindFragment(fragment: LinkCommentsFragment): Fragment

    @Binds
    fun bindMainView(fragment: LinkCommentsFragment): MainView

    @Binds
    fun bindLinkCommentsView(fragment: LinkCommentsFragment): LinkCommentsView
}
