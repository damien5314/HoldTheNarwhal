package com.ddiehl.android.htn.settings

import dagger.Subcomponent

@Subcomponent(modules = [SettingsFragmentModule::class])
interface SettingsFragmentComponent {

    fun inject(fragment: SettingsFragment)
}
