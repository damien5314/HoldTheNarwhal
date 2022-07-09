package com.ddiehl.android.htn.gallery

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaGalleryItem(
    val url: String,
) : Parcelable
