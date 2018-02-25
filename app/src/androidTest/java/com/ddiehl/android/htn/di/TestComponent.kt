package com.ddiehl.android.htn.di

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
    InstrumentationTestModule::class
])
interface TestComponent
