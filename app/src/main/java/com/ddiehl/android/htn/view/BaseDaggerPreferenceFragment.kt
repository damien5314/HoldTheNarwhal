package com.ddiehl.android.htn.view

import android.os.Bundle
import android.preference.PreferenceFragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

/**
 * Base PreferenceFragment class that calls into [AndroidInjection] to inject dependencies
 * from the Fragment's dagger subcomponent.
 */
abstract class BaseDaggerPreferenceFragment : PreferenceFragment(),
    HasAndroidInjector {

    @Inject
    internal lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun androidInjector(): AndroidInjector<Any> = androidInjector
}
