package com.ddiehl.android.htn.di

import com.ddiehl.android.htn.listings.report.ReportView
import com.ddiehl.android.htn.subscriptions.SubscriptionManagerFragment
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

    @FragmentScope
    @ContributesAndroidInjector(modules = [])
    fun bindSubscriptionManagerFragment(): SubscriptionManagerFragment
}
