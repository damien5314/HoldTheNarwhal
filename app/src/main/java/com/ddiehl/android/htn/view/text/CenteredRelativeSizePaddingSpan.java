package com.ddiehl.android.htn.view.text;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.style.ReplacementSpan;

import org.jetbrains.annotations.NotNull;

/**
 * https://stackoverflow.com/a/29023947/3238938
 */
public class CenteredRelativeSizePaddingSpan extends ReplacementSpan {

    private final float proportion;
    private final float padding;
    private final RectF rect = new RectF();

    public CenteredRelativeSizePaddingSpan(float proportion, float padding) {
        this.proportion = proportion;
        this.padding = padding;
    }

    @Override
    public void draw(
            @NotNull Canvas canvas,
            CharSequence text,
            int start,
            int end,
            float x,
            int top,
            int y,
            int bottom,
            @NotNull Paint paint) {
        final int originalColor = paint.getColor();

        paint.setColor(Color.TRANSPARENT);
        final float textWidth = paint.measureText(text, start, end);
        rect.set(x, top, x + textWidth + padding, bottom);
        canvas.drawRect(rect, paint);

        paint.setColor(originalColor);
        int xPos = Math.round(x + (padding / 2));
        int yPos = (int) paint.getTextSize() / 2;
        canvas.drawText(text, start, end, xPos, yPos, paint);
    }

    @Override
    public int getSize(
            @NotNull Paint paint,
            CharSequence text,
            int start,
            int end,
            Paint.FontMetricsInt fm) {
        final float textWidth = paint.measureText(text, start, end);
        return Math.round(textWidth + padding);
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
        shift += bounds.bottom - bounds.top;
        shift /= 2;
        ds.baselineShift += shift;
    }
}
