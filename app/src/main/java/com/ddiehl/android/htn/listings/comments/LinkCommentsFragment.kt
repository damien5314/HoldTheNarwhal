package com.ddiehl.android.htn.listings.comments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ddiehl.android.htn.HoldTheNarwhal
import com.ddiehl.android.htn.R
import com.ddiehl.android.htn.listings.BaseListingsFragment
import com.ddiehl.android.htn.settings.SettingsManager
import com.ddiehl.android.htn.utils.AndroidUtils.safeStartActivity
import com.hannesdorfmann.fragmentargs.FragmentArgs
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
import rxreddit.model.Comment
import rxreddit.model.Link
import rxreddit.model.Listing
import javax.inject.Inject

@FragmentWithArgs
class LinkCommentsFragment : BaseListingsFragment(), LinkCommentsView,
    SwipeRefreshLayout.OnRefreshListener {

    companion object {
        @JvmField
        val TAG: String = LinkCommentsFragment::class.java.simpleName
    }

    @field:Inject
    lateinit var settingsManager: SettingsManager

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
        HoldTheNarwhal.getApplicationComponent().inject(this)
        FragmentArgs.inject(this)
        presenter = LinkCommentsPresenter(this, redditNavigationView, this)
        listingsPresenter = presenter
        sort = settingsManager.commentSort
        callbacks = listingsPresenter
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

    override fun openShareView(comment: Comment) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, comment.url)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    override fun openCommentInBrowser(comment: Comment) {
        val uri = Uri.parse(comment.url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        safeStartActivity(context, intent)
    }

    override fun openShareView(link: Link) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://www.reddit.com" + link.permalink)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    override fun openUserProfileView(link: Link) {
        redditNavigationView.showUserProfile(link.author, null, null)
    }

    override fun openUserProfileView(comment: Comment) {
        redditNavigationView.showUserProfile(comment.author, null, null)
    }

    override fun openLinkInBrowser(link: Link) {
        val uri = Uri.parse(link.url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        safeStartActivity(context, intent)
    }

    override fun openCommentsInBrowser(link: Link) {
        val uri = Uri.parse("https://www.reddit.com" + link.permalink)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        safeStartActivity(context, intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CHOOSE_SORT -> if (resultCode == RESULT_OK) {
                data?.getStringExtra(ChooseCommentSortDialog.EXTRA_SORT)?.let { sort ->
                    onSortSelected(sort)
                }
            }
            REQUEST_ADD_COMMENT -> if (resultCode == RESULT_OK) {
                data?.getStringExtra(AddCommentDialog.EXTRA_COMMENT_TEXT)?.let { commentText ->
                    presenter.onCommentSubmitted(commentText)
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
        chooseLinkSortDialog.show(fragmentManager!!, ChooseCommentSortDialog.TAG)
    }

    private fun onSortSelected(sort: String) {
        if (this.sort == sort) return

        this.sort = sort
        listingsPresenter.onSortChanged()
    }

    override fun openUrlInWebView(url: String) {
        redditNavigationView.openURL(url)
    }

    override fun showCommentsForLink(subreddit: String, linkId: String, commentId: String?) {
        redditNavigationView.showCommentsForLink(subreddit, linkId, commentId)
    }

    override fun openReplyView(listing: Listing) {
        val id = "${listing.kind}_${listing.id}"
        val dialog = AddCommentDialog.newInstance(id)
        dialog.setTargetFragment(this, REQUEST_ADD_COMMENT)
        dialog.show(fragmentManager!!, AddCommentDialog.TAG)
    }

    override fun onRefresh() {
        swipeRefreshLayout.isRefreshing = false
        presenter.refreshData()
    }

    override fun getChromeView(): View = coordinatorLayout

    override fun showLinkContextMenu(menu: ContextMenu, view: View, link: Link) {
        super.showLinkContextMenu(menu, view, link)

        // Show the reply action when viewing link comments
        menu.findItem(R.id.action_link_reply).isVisible = true
    }

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
