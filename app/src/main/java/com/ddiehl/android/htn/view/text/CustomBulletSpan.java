package com.ddiehl.android.htn.view.text;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.text.Layout;
import android.text.style.BulletSpan;

/**
 * Custom BulletSpan class used to adjust the leading margin on bullets.
 */
public class CustomBulletSpan extends BulletSpan {

    private static final int BULLET_RADIUS = 3; // Copied from super class

    private final int gapWidth;

    public CustomBulletSpan() {
        super();
        gapWidth = STANDARD_GAP_WIDTH;
    }

    public CustomBulletSpan(int gapWidth) {
        super(gapWidth);
        this.gapWidth = gapWidth;
    }

    public CustomBulletSpan(int gapWidth, int color) {
        super(gapWidth, color);
        this.gapWidth = gapWidth;
    }

    public CustomBulletSpan(Parcel src) {
        super(src);
        gapWidth = src.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(gapWidth);
    }

    @Override
    public int getLeadingMargin(boolean first) {
        return 2 * BULLET_RADIUS + gapWidth;
    }

    @Override
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
                                  int top, int baseline, int bottom,
                                  CharSequence text, int start, int end,
                                  boolean first, Layout l) {
        super.drawLeadingMargin(
                c, p,
                x + gapWidth / 2, // To add a larger leading margin
                dir, top, baseline, bottom, text, start, end, first, l
        );
    }
}
