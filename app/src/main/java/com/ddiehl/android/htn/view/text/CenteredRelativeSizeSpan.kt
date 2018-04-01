package com.ddiehl.android.htn.view.text

import android.graphics.Rect
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

/**
 * https://stackoverflow.com/a/37088653/3238938
 */
class CenteredRelativeSizeSpan(val sizeChange: Float) : MetricAffectingSpan() {

    override fun updateDrawState(ds: TextPaint) {
        updateAnyState(ds)
    }

    override fun updateMeasureState(ds: TextPaint) {
        updateAnyState(ds)
    }

    private fun updateAnyState(ds: TextPaint) {
        val bounds = Rect()
        ds.getTextBounds("1A", 0, 2, bounds)
        var shift = bounds.top - bounds.bottom
        ds.textSize = ds.textSize * sizeChange
        ds.getTextBounds("1A", 0, 2, bounds)
        shift -= bounds.top - bounds.bottom
        shift /= 2
        ds.baselineShift += shift
    }
}
