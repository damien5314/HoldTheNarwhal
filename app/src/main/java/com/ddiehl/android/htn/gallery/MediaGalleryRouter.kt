package com.ddiehl.android.htn.gallery

import androidx.fragment.app.FragmentActivity
import rxreddit.model.GalleryItem
import timber.log.Timber
import javax.inject.Inject

/**
 * TODO: Describe what this class is responsible for
 */
class MediaGalleryRouter @Inject constructor(
    private val activity: FragmentActivity,
) {

    fun openLinkGallery(galleryItems: List<GalleryItem>) {
        Timber.d("Opening gallery with item count: %s", galleryItems.size)
        MediaGalleryFragment.create(galleryItems)
            .show(activity.supportFragmentManager, MediaGalleryFragment.TAG)
    }
}
