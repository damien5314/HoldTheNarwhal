package com.ddiehl.android.htn.view.text;

import android.graphics.Rect;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

/**
 * https://stackoverflow.com/a/37088653/3238938
 */
public class CenteredRelativeSizeSpan extends MetricAffectingSpan {

    private final float proportion;

    public CenteredRelativeSizeSpan(float proportion) {
        this.proportion = proportion;
    }

    public float getSizeChange() {
        return proportion;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        updateAnyState(ds);
    }

    @Override
    public void updateMeasureState(TextPaint ds) {
        updateAnyState(ds);
    }

    private void updateAnyState(TextPaint ds) {
        Rect bounds = new Rect();
        ds.getTextBounds("1A", 0, 2, bounds);
        int shift = bounds.top - bounds.bottom;
//        Timber.d("[dcd] normal text size --> top %s bottom %s shift %s",
//                bounds.top, bounds.bottom, shift1);
        ds.setTextSize(ds.getTextSize() * proportion);
        ds.getTextBounds("1A", 0, 2, bounds);
        shift -= bounds.top - bounds.bottom;
//        Timber.d("[dcd] scaled text size --> top %s bottom %s shift %s",
//                bounds.top, bounds.bottom, shift2);
        shift /= 2;
//        Timber.d("[dcd] shift --> %s", shift);
        ds.baselineShift += shift;
//        Timber.d("[dcd] baseline shift --> %s", ds.baselineShift);
    }
}
