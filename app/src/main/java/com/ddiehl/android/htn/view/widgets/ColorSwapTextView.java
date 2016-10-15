package com.ddiehl.android.htn.view.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

public class ColorSwapTextView extends TextView {

    private ColorStateList mOriginalTextColor;
    private ColorStateList mOriginalHintTextColor;
    private ColorStateList mOriginalLinkTextColor;
    private Drawable mOriginalBackground;

    public ColorSwapTextView(Context context) {
        super(context);
        init();
    }

    public ColorSwapTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorSwapTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ColorSwapTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mOriginalTextColor = getTextColors();
        mOriginalHintTextColor = getHintTextColors();
        mOriginalLinkTextColor = getLinkTextColors();
        mOriginalBackground = getBackground();
    }

    public ColorStateList getOriginalTextColor() {
        return mOriginalTextColor;
    }

    public ColorStateList getOriginalHintTextColor() {
        return mOriginalHintTextColor;
    }

    public ColorStateList getOriginalLinkTextColor() {
        return mOriginalLinkTextColor;
    }

    public Drawable getOriginalBackground() {
        return mOriginalBackground;
    }
}
