package com.ddiehl.android.htn.listings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import butterknife.BindView
import butterknife.ButterKnife
import com.ddiehl.android.htn.HoldTheNarwhal
import com.ddiehl.android.htn.R
import com.ddiehl.android.htn.listings.report.ReportActivity
import com.ddiehl.android.htn.listings.report.ReportActivity.RESULT_REPORT_ERROR
import com.ddiehl.android.htn.listings.report.ReportActivity.RESULT_REPORT_SUCCESS
import com.ddiehl.android.htn.listings.subreddit.SubredditActivity
import com.ddiehl.android.htn.utils.AndroidUtils.safeStartActivity
import com.ddiehl.android.htn.view.BaseFragment
import rxreddit.model.Comment
import rxreddit.model.Link
import rxreddit.model.Listing
import rxreddit.model.PrivateMessage

abstract class BaseListingsFragment : BaseFragment(), ListingsView, SwipeRefreshLayout.OnRefreshListener {

    companion object {
        private val REQUEST_REPORT_LISTING = 1000
        private val LINK_BASE_URL = "http://www.reddit.com"
    }

    @BindView(R.id.recycler_view)
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
        HoldTheNarwhal.getApplicationComponent().inject(this)
        retainInstance = true
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        return super.onCreateView(inflater, container, state).also {
            instantiateListView()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefreshLayout = ButterKnife.findById(view, R.id.swipe_refresh_layout)
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
                redditNavigationView.showSettings()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_REPORT_LISTING -> when (resultCode) {
                RESULT_REPORT_SUCCESS -> showReportSuccessToast(listingSelected!!)
                RESULT_REPORT_ERROR -> showReportErrorToast(listingSelected!!)
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun showReportSuccessToast(listing: Listing) {
        Snackbar.make(chromeView, R.string.report_successful, Snackbar.LENGTH_LONG).show()
    }

    private fun showReportErrorToast(listing: Listing) {
        Snackbar.make(chromeView, R.string.report_error, Snackbar.LENGTH_LONG).show()
    }

    open fun showLinkContextMenu(menu: ContextMenu, view: View, link: Link) {
        listingSelected = link
        activity?.menuInflater?.inflate(R.menu.link_context, menu)

        // Build title for menu
        val score = if (link.score == null) {
            view.context.getString(R.string.hidden_score_placeholder)
        } else {
            link.score.toString()
        }
        val title = getString(R.string.menu_action_link).format(link.title, score)
        menu.setHeaderTitle(title)

        // Set state of hide/unhide
        menu.findItem(R.id.action_link_hide).isVisible = !link.isHidden
        menu.findItem(R.id.action_link_unhide).isVisible = link.isHidden

        // Set subreddit for link in the view subreddit menu item
        val subreddit = getString(R.string.action_view_subreddit).format(link.subreddit)
        menu.findItem(R.id.action_link_view_subreddit).title = subreddit

        // Set username for link in the view user profile menu item
        val username = getString(R.string.action_view_user_profile).format(link.author)
        menu.findItem(R.id.action_link_view_user_profile).title = username

        // Hide user profile for posts by deleted users
        if ("[deleted]".equals(link.author, ignoreCase = true)) {
            menu.findItem(R.id.action_link_view_user_profile).isVisible = false
        }

        menu.findItem(R.id.action_link_reply).isVisible = false
        menu.findItem(R.id.action_link_save).isVisible = !link.isSaved
        menu.findItem(R.id.action_link_unsave).isVisible = link.isSaved
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
        activity!!.menuInflater.inflate(R.menu.message_context, menu)

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
                listingsPresenter.reportLink(listingSelected as Link)
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

    open fun openShareView(link: Link) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, LINK_BASE_URL + link.permalink)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    open fun openLinkInBrowser(link: Link) {
        val uri = Uri.parse(link.url)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        safeStartActivity(context, intent)
    }

    open fun openCommentsInBrowser(link: Link) {
        val uri = Uri.parse(LINK_BASE_URL + link.permalink)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        safeStartActivity(context, intent)
    }

    open fun openUrlInWebView(url: String) {
        redditNavigationView.openURL(url)
    }

    open fun openVideoInDialog(url: String) {

    }

    open fun showCommentsForLink(
            subreddit: String, linkId: String, commentId: String?) {
        redditNavigationView.showCommentsForLink(subreddit, linkId, commentId)
    }

    open fun openReplyView(listing: Listing) {
        showToast(getString(R.string.implementation_pending))
    }

    open fun openShareView(comment: Comment) {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_TEXT, comment.url)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }

    fun openSubredditView(subreddit: String) {
        val intent = SubredditActivity.getIntent(context, subreddit, null, null)
        startActivity(intent)
    }

    open fun openUserProfileView(link: Link) {
        redditNavigationView.showUserProfile(link.author, null, null)
    }

    open fun openUserProfileView(comment: Comment) {
        redditNavigationView.showUserProfile(comment.author, null, null)
    }

    open fun openCommentInBrowser(comment: Comment) {
        val uri = Uri.parse(comment.url)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        safeStartActivity(context, intent)
    }

    fun openReportView(link: Link) {
        val intent = ReportActivity.getIntent(context, link.fullName, link.subreddit)
        startActivityForResult(intent, REQUEST_REPORT_LISTING)
    }

    fun openReportView(comment: Comment) {
        val intent = ReportActivity.getIntent(context, comment.fullName, comment.subreddit)
        startActivityForResult(intent, REQUEST_REPORT_LISTING)
    }

    fun openReportView(message: PrivateMessage) {
        val intent = ReportActivity.getIntent(context, message.fullname, null)
        startActivityForResult(intent, REQUEST_REPORT_LISTING)
    }

    override fun notifyDataSetChanged() = listingsAdapter.notifyDataSetChanged()
    override fun notifyItemChanged(position: Int) = listingsAdapter.notifyItemChanged(position)
    override fun notifyItemInserted(position: Int) = listingsAdapter.notifyItemInserted(position)
    override fun notifyItemRemoved(position: Int) = listingsAdapter.notifyItemRemoved(position)
    override fun notifyItemRangeChanged(position: Int, count: Int) = listingsAdapter.notifyItemRangeChanged(position, count)
    override fun notifyItemRangeInserted(position: Int, count: Int) = listingsAdapter.notifyItemRangeInserted(position, count)
    override fun notifyItemRangeRemoved(position: Int, count: Int) = listingsAdapter.notifyItemRangeRemoved(position, count)

    override fun onRefresh() {
        swipeRefreshLayout.isRefreshing = false
        listingsPresenter.refreshData()
    }
}
