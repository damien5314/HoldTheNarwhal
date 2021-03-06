package com.ddiehl.android.htn.view.text;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.style.QuoteSpan;

import androidx.annotation.ColorInt;

/**
 * A {@link QuoteSpan} implementation that overrides {@link QuoteSpan#STRIPE_WIDTH}
 * and {@link QuoteSpan#GAP_WIDTH}.
 */
public class CustomQuoteSpan extends QuoteSpan {

    private static final int STRIPE_WIDTH = 4;
    private static final int GAP_WIDTH = 16;

    public CustomQuoteSpan(@ColorInt int color) {
        super(color);
    }

    @Override
    public int getLeadingMargin(boolean first) {
        return STRIPE_WIDTH + GAP_WIDTH;
    }

    @Override
    public void drawLeadingMargin(
            Canvas c, Paint p, int x, int dir,
            int top, int baseline, int bottom,
            CharSequence text, int start, int end,
            boolean first, Layout layout) {
        Paint.Style style = p.getStyle();
        int color = p.getColor();

        p.setStyle(Paint.Style.FILL);
        p.setColor(getColor());

        c.drawRect(x, top, x + dir * STRIPE_WIDTH, bottom, p);

        p.setStyle(style);
        p.setColor(color);
    }
}
