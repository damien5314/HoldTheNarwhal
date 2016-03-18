package com.ddiehl.android.htn.view.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

public class ColorSwapTextView extends TextView {

  private ColorStateList mOriginalTextColor;
  private ColorStateList mOriginalHintTextColor;
  private ColorStateList mOriginalLinkTextColor;
  private Drawable mOriginalBackground;

  public ColorSwapTextView(Context context) {
    this(context, null, 0, 0);
  }

  public ColorSwapTextView(Context context, AttributeSet attrs) {
    this(context, attrs, 0, 0);
  }

  public ColorSwapTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, 0);
  }

  @SuppressLint("NewApi")
  public ColorSwapTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
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
