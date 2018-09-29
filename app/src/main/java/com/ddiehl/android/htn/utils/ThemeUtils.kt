package com.ddiehl.android.htn.utils

import android.content.Context
import android.support.annotation.AttrRes
import android.support.annotation.ColorInt
import android.util.TypedValue

/**
 * Resolves the color value associated with a theme attribute.
 */
@ColorInt
fun getColorFromAttr(context: Context, @AttrRes colorAttr: Int): Int {
    val typedValue = TypedValue()
    val theme = context.getTheme()
    theme.resolveAttribute(colorAttr, typedValue, true)
    return typedValue.data
}
