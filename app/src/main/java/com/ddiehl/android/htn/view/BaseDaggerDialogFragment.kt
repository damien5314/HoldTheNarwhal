package com.ddiehl.android.htn.view

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * Base DialogFragment class that calls into [AndroidInjection] to inject dependencies
 * from the Fragment's dagger subcomponent.
 */
abstract class BaseDaggerDialogFragment : DialogFragment(),
    HasAndroidInjector {

    @Inject
    internal lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun androidInjector(): AndroidInjector<Any> = androidInjector
}
