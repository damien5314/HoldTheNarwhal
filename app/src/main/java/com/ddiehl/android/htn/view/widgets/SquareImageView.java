package com.ddiehl.android.htn.view.widgets;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class SquareImageView  extends AppCompatImageView {

  public SquareImageView(Context context) {
    super(context);
  }

  public SquareImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onMeasure(int width, int height) {
    //noinspection SuspiciousNameCombination
    super.onMeasure(height, height);
  }
}