package com.ddiehl.android.htn.view.markdown

import android.support.test.InstrumentationRegistry
import android.text.SpannableStringBuilder
import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlParserTests {

    @Test
    fun trimTrailingNewLines_emptyString_noCrash() {
        val context = InstrumentationRegistry.getTargetContext()

        val htmlParser = HtmlParser(context)
        val spannableStringBuilder = SpannableStringBuilder("")
        htmlParser.trimTrailingNewLines(spannableStringBuilder)

        assertTrue(spannableStringBuilder.isEmpty())
    }

    @Test
    fun trimTrailingNewLines_newlineOnly_noCrash() {
        val context = InstrumentationRegistry.getTargetContext()

        val htmlParser = HtmlParser(context)
        val spannableStringBuilder = SpannableStringBuilder("\n\n\n")
        htmlParser.trimTrailingNewLines(spannableStringBuilder)

        assertTrue(spannableStringBuilder.isEmpty())
    }
}
