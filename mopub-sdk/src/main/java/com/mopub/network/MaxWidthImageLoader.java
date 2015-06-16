package com.mopub.network;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import com.mopub.volley.RequestQueue;

public class MaxWidthImageLoader extends com.mopub.volley.toolbox.ImageLoader {
    private final int mMaxImageWidth;


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public MaxWidthImageLoader(final RequestQueue queue, final Context context, final ImageCache imageCache) {
        super(queue, imageCache);

        // Get Display Options
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
            size.set(display.getWidth(), display.getHeight());
        } else {
            display.getSize(size);
        }

        // Make our images no wider than the skinny side of the display.
        mMaxImageWidth = Math.min(size.x, size.y);
    }

    @Override
    public ImageContainer get(final String requestUrl, final ImageListener listener) {
        return super.get(requestUrl, listener, mMaxImageWidth, 0 /* no height limit */);
    }
}
