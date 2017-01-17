package com.ddiehl.android.htn.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.View;

import static com.ddiehl.android.htn.utils.AndroidUtils.safeStartActivity;

/**
 * http://stackoverflow.com/questions/4096851/remove-underline-from-links-in-textview-android
 * http://stackoverflow.com/questions/35944727/android-handle-override-interrupt-intent-from-the-same-activity-that-fired-it
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

    @Override
    public void onClick(View widget) {
        Uri uri = Uri.parse(getURL());
        Context context = widget.getContext();
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
        safeStartActivity(widget.getContext(), intent);
    }
}
