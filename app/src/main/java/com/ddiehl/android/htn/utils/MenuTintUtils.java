package com.ddiehl.android.htn.utils;

import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.ddiehl.android.htn.R;

import androidx.core.graphics.drawable.DrawableCompat;

/**
 * Tints menu icons similar to the AppCompat style
 * <p>
 * http://stackoverflow.com/a/33697621/3238938
 */
public class MenuTintUtils {

    public static void tintAllIcons(Menu menu, final int color) {
        for (int i = 0; i < menu.size(); ++i) {
            final MenuItem item = menu.getItem(i);
            tintMenuItemIcon(color, item);
            tintShareIconIfPresent(color, item);
        }
    }

    private static void tintMenuItemIcon(int color, MenuItem item) {
        final Drawable drawable = item.getIcon();
        if (drawable != null) {
            final Drawable wrapped = DrawableCompat.wrap(drawable);
            drawable.mutate();
            DrawableCompat.setTint(wrapped, color);
            item.setIcon(drawable);
        }
    }

    private static void tintShareIconIfPresent(int color, MenuItem item) {
        if (item.getActionView() != null) {
            final View actionView = item.getActionView();
            final View expandActivitiesButton = actionView.findViewById(R.id.expand_activities_button);
            if (expandActivitiesButton != null) {
                final ImageView image = (ImageView) expandActivitiesButton.findViewById(R.id.image);
                if (image != null) {
                    final Drawable drawable = image.getDrawable();
                    final Drawable wrapped = DrawableCompat.wrap(drawable);
                    drawable.mutate();
                    DrawableCompat.setTint(wrapped, color);
                    image.setImageDrawable(drawable);
                }
            }
        }
    }
}
