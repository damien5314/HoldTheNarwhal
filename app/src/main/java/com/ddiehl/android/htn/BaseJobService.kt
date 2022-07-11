package com.ddiehl.android.htn

import android.app.job.JobService
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

/**
 * TODO: Describe what this class is responsible for
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
