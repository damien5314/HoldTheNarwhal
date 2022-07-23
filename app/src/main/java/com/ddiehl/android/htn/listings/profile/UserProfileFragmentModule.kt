package com.ddiehl.android.htn.listings.profile

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.ddiehl.android.htn.view.MainView
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
interface UserProfileFragmentModule {

    companion object {

        @Provides
        fun bindFragmentActivity(fragment: UserProfileFragment): FragmentActivity = fragment.requireActivity()
    }

    @Binds
    fun bindFragment(fragment: UserProfileFragment): Fragment

    @Binds
    fun bindMainView(fragment: UserProfileFragment): MainView

    @Binds
    fun bindUserProfileView(fragment: UserProfileFragment): UserProfileView
}
