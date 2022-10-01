package com.ddiehl.android.htn.gallery

import androidx.fragment.app.FragmentActivity
import rxreddit.model.GalleryItem
import timber.log.Timber
import javax.inject.Inject

class MediaGalleryRouter @Inject constructor() {

    fun openLinkGallery(activity: FragmentActivity, galleryItems: List<GalleryItem>) {
        if (activity.isDestroyed) {
            Timber.w(IllegalStateException("Attempting to open gallery but Activity is destroyed"))
            return
        }

        Timber.d("Opening gallery with item count: %s", galleryItems.size)
        MediaGalleryFragment.create(galleryItems)
            .show(activity.supportFragmentManager, MediaGalleryFragment.TAG)
    }
}
