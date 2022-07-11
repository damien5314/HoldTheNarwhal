package com.ddiehl.android.htn.view

import android.os.Bundle
import android.os.PersistableBundle
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

/**
 * Base Activity class that calls into [AndroidInjection] to inject dependencies
 * from the Activity's dagger subcomponent.
 */
abstract class BaseDaggerActivity : BaseActivity(),
    HasAndroidInjector {

    @Inject
    internal lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState, persistentState)
    }

    override fun androidInjector(): AndroidInjector<Any> = androidInjector
}
