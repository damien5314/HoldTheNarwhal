package com.ddiehl.android.htn.utils

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt

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
