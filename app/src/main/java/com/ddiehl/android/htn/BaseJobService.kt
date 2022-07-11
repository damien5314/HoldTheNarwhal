package com.ddiehl.android.htn

import android.app.job.JobService
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

/**
 * Base JobService class that calls into [AndroidInjection] to inject dependencies
 * from the Service's dagger subcomponent.
 */
abstract class BaseJobService : JobService(),
    HasAndroidInjector {

    @Inject
    internal lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun androidInjector(): AndroidInjector<Any> = androidInjector
}
