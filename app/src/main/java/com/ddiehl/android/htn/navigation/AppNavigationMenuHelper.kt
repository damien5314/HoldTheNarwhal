package com.ddiehl.android.htn.navigation

import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import com.ddiehl.android.htn.R
import com.ddiehl.android.htn.view.glide.GlideApp
import javax.inject.Inject

/**
 * TODO: Describe what this class is responsible for
 */
class AppNavigationMenuHelper @Inject constructor(
    private val activity: FragmentActivity,
) {

    fun loadImageIntoDrawerHeader(url: String?) {
        val headerImage = activity.findViewById<ImageView>(R.id.navigation_drawer_header_image)
        GlideApp.with(activity)
            .load(url)
            .into(headerImage)
    }
}
