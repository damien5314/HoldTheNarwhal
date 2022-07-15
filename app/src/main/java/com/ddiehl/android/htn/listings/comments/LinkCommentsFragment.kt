package com.ddiehl.android.htn.listings.comments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ddiehl.android.htn.R
import com.ddiehl.android.htn.gallery.MediaGalleryRouter
import com.ddiehl.android.htn.listings.BaseListingsFragment
import com.ddiehl.android.htn.settings.SettingsManager
import com.ddiehl.android.htn.view.video.VideoPlayerRouter
import com.hannesdorfmann.fragmentargs.FragmentArgs
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import javax.inject.Inject

@FragmentWithArgs
class LinkCommentsFragment : BaseListingsFragment(), LinkCommentsView,
    SwipeRefreshLayout.OnRefreshListener {

    companion object {
        @JvmField
        val TAG: String = LinkCommentsFragment::class.java.simpleName
    }

    @Inject
    lateinit var settingsManager: SettingsManager
    @Inject
    lateinit var linkCommentsRouter: LinkCommentsRouter
    @Inject
    lateinit var mediaGalleryRouter: MediaGalleryRouter
    @Inject
    lateinit var addCommentDialogRouter: AddCommentDialogRouter
    @Inject
    lateinit var videoPlayRouter: VideoPlayerRouter

    @field:Arg
    override lateinit var subreddit: String

    @field:Arg
    override lateinit var articleId: String

    @field:Arg
    override var commentId: String? = null

    private lateinit var coordinatorLayout: CoordinatorLayout

    override lateinit var sort: String

    private lateinit var presenter: LinkCommentsPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FragmentArgs.inject(this)
        presenter = LinkCommentsPresenter(
            this,
            appRouter,
            linkCommentsRouter,
            mediaGalleryRouter,
            videoPlayRouter,
            addCommentDialogRouter,
            reportViewRouter,
            this,
        )
        listingsPresenter = presenter
        sort = settingsManager.commentSort
        callbacks = listingsPresenter

        listenForAddCommentResult()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        coordinatorLayout = view.findViewById(R.id.coordinator_layout);

        val title = getString(R.string.link_subreddit).format(subreddit)
        setTitle(title)
    }

    override fun onDestroy() {
        presenter.clearData()
        notifyDataSetChanged()

        super.onDestroy()
    }

    override fun getLayoutResId(): Int {
        return R.layout.link_comments_fragment
    }

    override val listingsAdapter by lazy { LinkCommentsAdapter(this, presenter) }

    private fun listenForAddCommentResult() {
        addCommentDialogRouter.observeResults()
            .subscribe { result ->
                when (result) {
                    is AddCommentDialogRouter.Result.Success -> presenter.onCommentSubmitted(result.commentText)
                    AddCommentDialogRouter.Result.Canceled -> {
                        // no-op
                    }
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CHOOSE_SORT -> if (resultCode == RESULT_OK) {
                data?.getStringExtra(ChooseCommentSortDialog.EXTRA_SORT)?.let { sort ->
                    onSortSelected(sort)
                }
            }
        }
    }

    override fun refreshOptionsMenu() {
        val activity = activity
        activity?.invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.comments, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val link = presenter.linkContext

        val viewSubredditItem = menu.findItem(R.id.action_link_view_subreddit)
        val viewUserItem = menu.findItem(R.id.action_link_view_user_profile)

        if (link == null) {
            viewSubredditItem.isVisible = false
            viewUserItem.isVisible = false
        } else {
            viewSubredditItem.isVisible = true
            viewUserItem.isVisible = true

            // Insert name of subreddit to its menu item
            val viewSubredditTitle = viewSubredditItem.title.toString()
            viewSubredditItem.title = String.format(viewSubredditTitle, link.subreddit)

            // Insert name of user in user profile menu item
            val viewUserTitle = viewUserItem.title.toString()
            viewUserItem.title = String.format(viewUserTitle, link.author)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val link = presenter.linkContext

        when (item.itemId) {
            R.id.action_share -> {
                listingsPresenter.shareLink(link)
                return true
            }
            R.id.action_change_sort -> {
                showSortOptionsMenu()
                return true
            }
            R.id.action_link_view_subreddit -> {
                listingsPresenter.openLinkSubreddit(link)
                return true
            }
            R.id.action_link_view_user_profile -> {
                listingsPresenter.openLinkUserProfile(link)
                return true
            }
            R.id.action_link_open_in_browser -> {
                listingsPresenter.openLinkInBrowser(link)
                return true
            }
            R.id.action_link_open_comments_in_browser -> {
                listingsPresenter.openCommentsInBrowser(link)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showSortOptionsMenu() {
        val chooseLinkSortDialog = ChooseCommentSortDialog.newInstance(sort)
        chooseLinkSortDialog.setTargetFragment(this, REQUEST_CHOOSE_SORT)
        chooseLinkSortDialog.show(parentFragmentManager, ChooseCommentSortDialog.TAG)
    }

    private fun onSortSelected(sort: String) {
        if (this.sort == sort) return

        this.sort = sort
        listingsPresenter.onSortChanged()
    }

    override fun onRefresh() {
        swipeRefreshLayout.isRefreshing = false
        presenter.refreshData()
    }

    override fun getChromeView(): View = coordinatorLayout

    /**
     * Overriding the below methods to account for the presence
     * of a link at the top of the adapter
     */
    override fun notifyItemChanged(position: Int) = super.notifyItemChanged(position + 1)

    override fun notifyItemInserted(position: Int) = super.notifyItemInserted(position + 1)

    override fun notifyItemRemoved(position: Int) = super.notifyItemRemoved(position + 1)

    override fun notifyItemRangeChanged(position: Int, count: Int) = super.notifyItemRangeChanged(position + 1, count)

    override fun notifyItemRangeInserted(position: Int, count: Int) = super.notifyItemRangeInserted(position + 1, count)

    override fun notifyItemRangeRemoved(position: Int, count: Int) = super.notifyItemRangeRemoved(position + 1, count)
}
