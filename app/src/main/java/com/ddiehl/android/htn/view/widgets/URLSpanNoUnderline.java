package com.ddiehl.android.htn.view.widgets;

import android.text.TextPaint;
import android.text.style.URLSpan;

/**
 * http://stackoverflow.com/questions/4096851/remove-underline-from-links-in-textview-android
 */
public class URLSpanNoUnderline extends URLSpan {

    public URLSpanNoUnderline(String url) {
        super(url);
    }

    @Override
    public void updateDrawState(TextPaint paint) {
        super.updateDrawState(paint);
        paint.setUnderlineText(false);
    }
}