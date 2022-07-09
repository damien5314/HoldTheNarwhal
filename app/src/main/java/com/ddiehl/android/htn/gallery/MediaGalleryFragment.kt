package com.ddiehl.android.htn.gallery

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.ddiehl.android.htn.HoldTheNarwhal
import com.ddiehl.android.htn.R
import com.ddiehl.android.htn.view.glide.GlideApp
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.ortiz.touchview.TouchImageView
import rxreddit.model.GalleryItem
import rxreddit.model.Link

/**
 * Fragment for displaying a UI to browse media in a gallery listing.
 *
 * See [Link.galleryItems] and [Link.isGallery].
 */
class MediaGalleryFragment : DialogFragment() {

    companion object {

        private const val ARG_GALLERY_ITEMS = "MediaGalleryFragment_GalleryItems"

        const val TAG = "MediaGalleryFragment"

        @JvmStatic
        fun create(
            galleryItems: List<GalleryItem>
        ): MediaGalleryFragment = MediaGalleryFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(
                    "MediaGalleryFragment_GalleryItems",
                    ArrayList(
                        galleryItems.map {
                            MediaGalleryItem(
                                url = it.url,
                            )
                        }
                    ),
                )
            }
        }
    }

    private val galleryItems: List<MediaGalleryItem> by lazy {
        requireArguments().getParcelableArrayList<MediaGalleryItem>(ARG_GALLERY_ITEMS) as List<MediaGalleryItem>
    }
    private val viewPager by lazy { requireView().findViewById<ViewPager2>(R.id.media_gallery_view_pager) }
    private val viewPagerTabs by lazy { requireView().findViewById<TabLayout>(R.id.media_gallery_view_pager_tabs) }
    private var tabLayoutMediator: TabLayoutMediator? = null

    init {
        HoldTheNarwhal.getApplicationComponent().inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        Dialog(requireContext(), R.style.DialogOverlay)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.media_gallery_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindMediaToViewPager(galleryItems)
    }

    private fun bindMediaToViewPager(galleryItems: List<MediaGalleryItem>) {
        this.tabLayoutMediator?.detach()

        viewPager.adapter = MediaGalleryViewPagerAdapter(galleryItems)
        tabLayoutMediator = TabLayoutMediator(viewPagerTabs, viewPager) { tab, position ->
            tab.text = (position + 1).toString()
        }
            .also { it.attach() }
    }

    private class MediaGalleryViewPagerAdapter(
        galleryItems: List<MediaGalleryItem>,
    ) : RecyclerView.Adapter<MediaGalleryItemViewHolder>() {

        private val galleryItems = ArrayList(galleryItems) // prevent others from modifying our list

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaGalleryItemViewHolder {
            return MediaGalleryItemViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.media_gallery_item, parent, false)
            )
        }

        override fun onBindViewHolder(holder: MediaGalleryItemViewHolder, position: Int) {
            galleryItems[position].let { galleryItem ->
                holder.bind(galleryItem)
            }
        }

        override fun getItemCount(): Int = galleryItems.size
    }

    private class MediaGalleryItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val itemImageView: TouchImageView = itemView.findViewById(R.id.media_gallery_item_image)

        fun bind(galleryItem: MediaGalleryItem) {
            itemImageView.resetZoom()
            GlideApp.with(itemView.context)
                .load(galleryItem.url)
                .into(itemImageView)
        }
    }
}
