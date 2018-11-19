package com.ddiehl.android.htn.view.theme

import androidx.annotation.StyleRes
import com.ddiehl.android.htn.R

/**
 * TODO documentation
 */
enum class ColorScheme(val id: String, @StyleRes val styleRes: Int) {
    STANDARD("standard", R.style.StandardThemeColors),
    NIGHT("night", R.style.NightThemeColors),
    ;

    companion object {

        @JvmStatic
        fun fromId(id: String): ColorScheme {
            ColorScheme.values().forEach {
                if (it.id == id) return it
            }
            throw IllegalArgumentException("No ColorScheme for ID: $id")
        }
    }
}
