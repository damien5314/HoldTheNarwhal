package com.ddiehl.android.htn.gallery

import android.app.Dialog
import android.os.Bundle
import android.view.*
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
import timber.log.Timber

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
    private lateinit var scaleGestureDetector: ScaleGestureDetector

    init {
        HoldTheNarwhal.getApplicationComponent().inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        Dialog(requireContext(), R.style.DialogOverlay).also {
            // TODO: Delete overloads you don't need
            this.scaleGestureDetector = ScaleGestureDetector(
                requireContext(),
                object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    override fun onScale(detector: ScaleGestureDetector?): Boolean {
                        return super.onScale(detector)
                    }

                    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
                        Timber.d("[dcd] LOCKING user input for swiping")
                        viewPager.isUserInputEnabled = false
                        return false
                    }

                    override fun onScaleEnd(detector: ScaleGestureDetector?) {
                        Timber.d("[dcd] UNLOCKING user input for swiping")
                        super.onScaleEnd(detector)
                        viewPager.isUserInputEnabled = true
                    }
                })
        }

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

    override fun onStart() {
        super.onStart()
        viewPager.setOnTouchListener { _, event ->
            this.scaleGestureDetector?.onTouchEvent(event) ?: false
        }
    }

    override fun onStop() {
        super.onStop()
        // TODO: Remove gesture detector
        viewPager.setOnTouchListener(null)
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

        init {
            suppressParentScrollingDuringZoom()
        }

        // Adapted from https://github.com/MikeOrtiz/TouchImageView/issues/191
        private fun suppressParentScrollingDuringZoom() {
            itemImageView.setOnTouchListener { view, event ->
                //can scroll horizontally checks if there's still a part of the image
                //that can be scrolled until you reach the edge
                return@setOnTouchListener if (event.pointerCount >= 2 ||
                    view.canScrollHorizontally(1) && view.canScrollHorizontally(-1)
                ) {
                    //multi-touch event
                    when (event.action) {
                        MotionEvent.ACTION_DOWN,
                        MotionEvent.ACTION_MOVE,
                        -> {
                            // Disallow RecyclerView to intercept touch events.
                            itemView.parent.requestDisallowInterceptTouchEvent(true)
                            false // Disable touch on view
                        }
                        MotionEvent.ACTION_UP -> {
                            // Allow RecyclerView to intercept touch events.
                            itemView.parent.requestDisallowInterceptTouchEvent(false)
                            true
                        }
                        else -> true
                    }
                } else true
            }
        }

        fun bind(galleryItem: MediaGalleryItem) {
            itemImageView.resetZoom()
            GlideApp.with(itemView.context)
                .load(galleryItem.url)
                .into(itemImageView)
        }
    }
}
