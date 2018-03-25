package com.ddiehl.android.htn.view.text;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;

import org.jetbrains.annotations.NotNull;

/**
 * https://stackoverflow.com/a/29023947/3238938
 */
public class PaddingSpan extends ReplacementSpan {

    private final float padding;
    private final RectF rect = new RectF();

    public PaddingSpan(float padding) {
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
}
