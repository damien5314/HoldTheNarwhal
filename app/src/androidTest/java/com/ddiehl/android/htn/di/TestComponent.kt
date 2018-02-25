package com.ddiehl.android.htn.di

import com.ddiehl.android.htn.view.markdown.MarkdownParserTest
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
    InstrumentationTestModule::class
])
interface TestComponent {

    fun inject(test: MarkdownParserTest)
}
