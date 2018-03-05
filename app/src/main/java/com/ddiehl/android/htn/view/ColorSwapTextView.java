package com.ddiehl.android.htn.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

public class ColorSwapTextView extends TextView {

    private ColorStateList originalTextColor;
    private ColorStateList originalHintTextColor;
    private ColorStateList originalLinkTextColor;
    private Drawable originalBackground;

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
        originalTextColor = getTextColors();
        originalHintTextColor = getHintTextColors();
        originalLinkTextColor = getLinkTextColors();
        originalBackground = getBackground();
    }

    public ColorStateList getOriginalTextColor() {
        return originalTextColor;
    }

    public ColorStateList getOriginalHintTextColor() {
        return originalHintTextColor;
    }

    public ColorStateList getOriginalLinkTextColor() {
        return originalLinkTextColor;
    }

    public Drawable getOriginalBackground() {
        return originalBackground;
    }
}
