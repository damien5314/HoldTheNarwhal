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
        ds.setTextSize(ds.getTextSize() * proportion);
        ds.getTextBounds("1A", 0, 2, bounds);
        shift -= bounds.top - bounds.bottom;
        shift /= 2;
        ds.baselineShift += shift;
    }
}
