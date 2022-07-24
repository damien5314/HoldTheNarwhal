package com.ddiehl.android.htn.navigation

import android.app.Activity
import android.content.Context
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import com.ddiehl.android.htn.R
import com.ddiehl.android.htn.view.glide.GlideApp
import javax.inject.Inject

class AppNavigationMenuHelper @Inject constructor(
    private val activity: FragmentActivity,
) {

    private fun Context.isValidGlideContext() = this !is Activity || (!this.isDestroyed && !this.isFinishing)

    fun loadImageIntoDrawerHeader(url: String?) {
        if (!activity.isValidGlideContext()) return

        val headerImage = activity.findViewById<ImageView>(R.id.navigation_drawer_header_image) ?: return
        GlideApp.with(activity)
            .load(url)
            .into(headerImage)
    }
}
