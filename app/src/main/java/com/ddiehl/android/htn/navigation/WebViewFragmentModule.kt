package com.ddiehl.android.htn.navigation

import androidx.fragment.app.FragmentActivity
import dagger.Module
import dagger.Provides

@Module
class WebViewFragmentModule {

    @Provides
    fun bindFragmentActivity(fragment: WebViewFragment): FragmentActivity = fragment.requireActivity()
}
