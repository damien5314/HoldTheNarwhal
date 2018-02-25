package com.ddiehl.android.htn.di

import `in`.uncod.android.bypass.Bypass
import android.content.Context
import android.support.v4.content.ContextCompat
import com.ddiehl.android.htn.R
import com.ddiehl.android.htn.view.markdown.MarkdownParser
import dagger.Module
import dagger.Provides
import timber.log.Timber
import javax.inject.Singleton

@Module
class InstrumentationTestModule(context: Context) {

    private val mContext: Context = context.applicationContext

    @Provides
    fun providesContext() = mContext

    @Singleton
    @Provides
    fun providesBypass(context: Context): Bypass? {
        return try {
            val options = Bypass.Options()
            val blockQuoteColor = ContextCompat.getColor(context, R.color.markdown_quote_block)
            options.setBlockQuoteColor(blockQuoteColor)
            Bypass(context, options)
        } catch (error: UnsatisfiedLinkError) {
            Timber.w("Bypass is unavailable")
            null
        }
    }

    @Provides
    internal fun providesMarkdownParser(bypass: Bypass?): MarkdownParser? {
        return if (bypass != null) {
            MarkdownParser(bypass)
        } else {
            null
        }
    }
}
