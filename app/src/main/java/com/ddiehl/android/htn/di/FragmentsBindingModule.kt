package com.ddiehl.android.htn.di

import com.ddiehl.android.htn.listings.report.ReportView
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * TODO: Describe what this class is responsible for
 */
@Module
interface FragmentsBindingModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [])
    fun bindReportView(): ReportView
}
