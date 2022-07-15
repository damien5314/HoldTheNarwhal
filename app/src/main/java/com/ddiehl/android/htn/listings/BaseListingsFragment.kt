package com.ddiehl.android.htn.listings

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ddiehl.android.htn.R
import com.ddiehl.android.htn.listings.report.ReportViewRouter
import com.ddiehl.android.htn.routing.AppRouter
import com.ddiehl.android.htn.view.BaseFragment
import com.google.android.material.snackbar.Snackbar
import rxreddit.model.Comment
import rxreddit.model.Link
import rxreddit.model.Listing
import rxreddit.model.PrivateMessage
import javax.inject.Inject

abstract class BaseListingsFragment : BaseFragment(),
    ListingsView,
    SwipeRefreshLayout.OnRefreshListener {

    @Inject
    internal lateinit var appRouter: AppRouter
    @Inject
    internal lateinit var reportViewRouter: ReportViewRouter

    lateinit var recyclerView: RecyclerView
    protected lateinit var swipeRefreshLayout: SwipeRefreshLayout

    protected lateinit var listingsPresenter: BaseListingsPresenter
    protected abstract val listingsAdapter: ListingsAdapter
    protected lateinit var callbacks: ListingsView.Callbacks
    private var listingSelected: Listing? = null

    private val onScrollListener: RecyclerView.OnScrollListener
        get() = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                with(recyclerView.layoutManager as LinearLayoutManager) {
                    handleScroll(childCount, itemCount, findFirstVisibleItemPosition())
                }
            }

            private fun handleScroll(visible: Int, total: Int, firstVisible: Int) {
                if (firstVisible == 0) {
                    callbacks.onFirstItemShown()
                } else if (visible + firstVisible >= total) {
                    callbacks.onLastItemShown()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setHasOptionsMenu(true)
        listenForReportViewResults()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        return super.onCreateView(inflater, container, state).also {
            recyclerView = it.findViewById(R.id.recycler_view);
            instantiateListView()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener(this)
    }

    private fun instantiateListView() {
        with(recyclerView) {
            layoutManager = LinearLayoutManager(activity)
            clearOnScrollListeners()
            addOnScrollListener(onScrollListener)
            adapter = listingsAdapter
        }
    }

    override fun onStart() {
        super.onStart()

        // FIXME Do we need to check nextRequested here?
        if (!listingsPresenter.hasData()) {
            listingsPresenter.refreshData()
        }
    }

    override fun onDestroy() {
        recyclerView.adapter = null

        // To disable the memory dereferencing functionality just comment these lines
        listingsPresenter.clearData()

        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> {
                listingsPresenter.refreshData()
                return true
            }
            R.id.action_settings -> {
                appRouter.showSettings()
                return true
            }
        }
        return false
    }

    protected fun hideTimespanOptionIfUnsupported(menu: Menu, sort: String) {
        menu.findItem(R.id.action_change_sort).isVisible = true
        when (sort) {
            "controversial", "top" -> menu.findItem(R.id.action_change_timespan).isVisible = true
            "hot", "new", "rising" -> menu.findItem(R.id.action_change_timespan).isVisible = false
            else -> menu.findItem(R.id.action_change_timespan).isVisible = false
        }
    }

    open fun showLinkContextMenu(menu: ContextMenu, view: View, link: Link) {
        listingSelected = link
    }

    fun showCommentContextMenu(menu: ContextMenu, view: View, comment: Comment) {
        listingSelected = comment
        activity?.menuInflater?.inflate(R.menu.comment_context, menu)

        // Build title for menu
        val score = if (comment.score == null) {
            view.context.getString(R.string.hidden_score_placeholder)
        } else {
            comment.score!!.toString()
        }
        val title = getString(R.string.menu_action_comment).format(comment.author, score)
        menu.setHeaderTitle(title)

        // Set username for listing in the user profile menu item
        val username = getString(R.string.action_view_user_profile).format(comment.author)
        menu.findItem(R.id.action_comment_view_user_profile).title = username

        // Hide save/unsave option
        menu.findItem(R.id.action_comment_save).isVisible = !comment.isSaved
        menu.findItem(R.id.action_comment_unsave).isVisible = comment.isSaved

        // Hide user profile for posts by deleted users
        if ("[deleted]".equals(comment.author, ignoreCase = true)) {
            menu.findItem(R.id.action_comment_view_user_profile).isVisible = false
        }

        // Don't show parent menu option if there is no parent
        if (comment.linkId == comment.parentId) {
            menu.findItem(R.id.action_comment_parent).isVisible = false
        }
    }

    fun showMessageContextMenu(menu: ContextMenu, view: View, message: PrivateMessage) {
        listingSelected = message
        requireActivity().menuInflater.inflate(R.menu.message_context, menu)

        // Hide ride/unread option based on state
        menu.findItem(R.id.action_message_mark_read).isVisible = message.isUnread!!
        menu.findItem(R.id.action_message_mark_unread).isVisible = !message.isUnread
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_link_reply -> {
                listingsPresenter.replyToLink(listingSelected as Link)
                return true
            }
            R.id.action_link_upvote -> {
                listingsPresenter.upvoteLink(listingSelected as Link)
                return true
            }
            R.id.action_link_downvote -> {
                listingsPresenter.downvoteLink(listingSelected as Link)
                return true
            }
            R.id.action_link_show_comments -> {
                listingsPresenter.showCommentsForLink(listingSelected as Link)
                return true
            }
            R.id.action_link_save -> {
                listingsPresenter.saveLink(listingSelected as Link)
                return true
            }
            R.id.action_link_unsave -> {
                listingsPresenter.unsaveLink(listingSelected as Link)
                return true
            }
            R.id.action_link_share -> {
                listingsPresenter.shareLink(listingSelected as Link)
                return true
            }
            R.id.action_link_view_subreddit -> {
                listingsPresenter.openLinkSubreddit(listingSelected as Link)
                return true
            }
            R.id.action_link_view_user_profile -> {
                listingsPresenter.openLinkUserProfile(listingSelected as Link)
                return true
            }
            R.id.action_link_open_in_browser -> {
                listingsPresenter.openLinkInBrowser(listingSelected as Link)
                return true
            }
            R.id.action_link_open_comments_in_browser -> {
                listingsPresenter.openCommentsInBrowser(listingSelected as Link)
                return true
            }
            R.id.action_link_hide -> {
                listingsPresenter.hideLink(listingSelected as Link)
                return true
            }
            R.id.action_link_unhide -> {
                listingsPresenter.unhideLink(listingSelected as Link)
                return true
            }
            R.id.action_link_report -> {
                listingSelected?.let { listing ->
                    reportViewRouter.openReportView(listing.fullName)
                }
                return true
            }
            R.id.action_comment_permalink -> {
                listingsPresenter.openCommentPermalink(listingSelected as Comment)
                return true
            }
            R.id.action_comment_parent -> {
                listingsPresenter.openCommentParent(listingSelected as Comment)
                return true
            }
            R.id.action_comment_reply -> {
                listingsPresenter.replyToComment(listingSelected as Comment)
                return true
            }
            R.id.action_comment_upvote -> {
                listingsPresenter.upvoteComment(listingSelected as Comment)
                return true
            }
            R.id.action_comment_downvote -> {
                listingsPresenter.downvoteComment(listingSelected as Comment)
                return true
            }
            R.id.action_comment_save -> {
                listingsPresenter.saveComment(listingSelected as Comment)
                return true
            }
            R.id.action_comment_unsave -> {
                listingsPresenter.unsaveComment(listingSelected as Comment)
                return true
            }
            R.id.action_comment_share -> {
                listingsPresenter.shareComment(listingSelected as Comment)
                return true
            }
            R.id.action_comment_view_user_profile -> {
                listingsPresenter.openCommentUserProfile(listingSelected as Comment)
                return true
            }
            R.id.action_comment_open_in_browser -> {
                listingsPresenter.openCommentInBrowser(listingSelected as Comment)
                return true
            }
            R.id.action_comment_report -> {
                listingsPresenter.reportComment(listingSelected as Comment)
                return true
            }
            R.id.action_message_show_permalink -> {
                listingsPresenter.showMessagePermalink(listingSelected as PrivateMessage)
                return true
            }
            R.id.action_message_report -> {
                listingsPresenter.reportMessage(listingSelected as PrivateMessage)
                return true
            }
            R.id.action_message_block_user -> {
                listingsPresenter.blockUser(listingSelected as PrivateMessage)
                return true
            }
            R.id.action_message_mark_read -> {
                listingsPresenter.markMessageRead(listingSelected as PrivateMessage)
                return true
            }
            R.id.action_message_mark_unread -> {
                listingsPresenter.markMessageUnread(listingSelected as PrivateMessage)
                return true
            }
            R.id.action_message_reply -> {
                listingsPresenter.replyToMessage(listingSelected as PrivateMessage)
                return true
            }
            else -> throw IllegalArgumentException("no action associated with item: ${item.title}")
        }
    }

    override fun notifyDataSetChanged() = listingsAdapter.notifyDataSetChanged()
    override fun notifyItemChanged(position: Int) = listingsAdapter.notifyItemChanged(position)
    override fun notifyItemInserted(position: Int) = listingsAdapter.notifyItemInserted(position)
    override fun notifyItemRemoved(position: Int) = listingsAdapter.notifyItemRemoved(position)
    override fun notifyItemRangeChanged(position: Int, count: Int) =
        listingsAdapter.notifyItemRangeChanged(position, count)

    override fun notifyItemRangeInserted(position: Int, count: Int) =
        listingsAdapter.notifyItemRangeInserted(position, count)

    override fun notifyItemRangeRemoved(position: Int, count: Int) =
        listingsAdapter.notifyItemRangeRemoved(position, count)

    override fun onRefresh() {
        swipeRefreshLayout.isRefreshing = false
        listingsPresenter.refreshData()
    }

    private fun listenForReportViewResults() {
        reportViewRouter.observeReportResults()
            .subscribe { result ->
                when (result) {
                    ReportViewRouter.ReportResult.SUCCESS -> showReportSuccessToast()
                    ReportViewRouter.ReportResult.CANCELED -> showReportErrorToast()
                    null -> { }
                }
            }
    }

    private fun showReportSuccessToast() {
        Snackbar.make(chromeView, R.string.report_successful, Snackbar.LENGTH_LONG).show()
    }

    private fun showReportErrorToast() {
        Snackbar.make(chromeView, R.string.report_error, Snackbar.LENGTH_LONG).show()
    }
}
