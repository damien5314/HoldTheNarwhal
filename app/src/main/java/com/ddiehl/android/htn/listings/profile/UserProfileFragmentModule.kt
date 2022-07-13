package com.ddiehl.android.htn.listings.profile

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import dagger.Module
import dagger.Provides

@Module
class UserProfileFragmentModule {

    @Provides
    fun bindFragment(fragment: UserProfileFragment): Fragment = fragment

    @Provides
    fun bindFragmentActivity(fragment: UserProfileFragment): FragmentActivity = fragment.requireActivity()
}
