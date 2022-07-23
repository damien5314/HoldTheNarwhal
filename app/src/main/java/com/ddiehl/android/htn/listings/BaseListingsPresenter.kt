package com.ddiehl.android.htn.listings

import android.content.Context
import android.view.MenuItem
import com.ddiehl.android.htn.HoldTheNarwhal
import com.ddiehl.android.htn.R
import com.ddiehl.android.htn.gallery.MediaGalleryRouter
import com.ddiehl.android.htn.identity.IdentityManager
import com.ddiehl.android.htn.listings.comments.AddCommentDialogRouter
import com.ddiehl.android.htn.listings.comments.LinkCommentsRouter
import com.ddiehl.android.htn.listings.report.ReportViewRouter
import com.ddiehl.android.htn.listings.subreddit.ThumbnailMode
import com.ddiehl.android.htn.managers.NetworkConnectivityManager
import com.ddiehl.android.htn.routing.AppRouter
import com.ddiehl.android.htn.settings.SettingsManager
import com.ddiehl.android.htn.view.MainView
import com.ddiehl.android.htn.view.video.VideoPlayerRouter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.schedulers.Schedulers
import rxreddit.api.RedditService
import rxreddit.model.*
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

abstract class BaseListingsPresenter(
    main: MainView,
    appRouter: AppRouter,
    linkCommentsRouter: LinkCommentsRouter,
    mediaGalleryRouter: MediaGalleryRouter,
    videoPlayerRouter: VideoPlayerRouter,
    addCommentDialogRouter: AddCommentDialogRouter,
    reportViewRouter: ReportViewRouter,
    view: ListingsView,
) : ListingsView.Callbacks {

    @JvmField
    @Inject
    var context: Context? = null

    @JvmField
    @Inject
    var identityManager: IdentityManager? = null

    @JvmField
    @Inject
    var settingsManager: SettingsManager? = null

    @JvmField
    @Inject
    var redditService: RedditService? = null

    @JvmField
    @Inject
    var networkConnectivityManager: NetworkConnectivityManager? = null

    val listings: MutableList<Listing> = mutableListOf()
    private val listingsView: ListingsView

    @JvmField
    protected val mainView: MainView
    private val appRouter: AppRouter
    private val linkCommentsRouter: LinkCommentsRouter
    private val mediaGalleryRouter: MediaGalleryRouter
    private val videoPlayerRouter: VideoPlayerRouter
    private val addCommentDialogRouter: AddCommentDialogRouter
    private val reportViewRouter: ReportViewRouter
    @JvmField
    protected var beforeRequested = false

    @JvmField
    protected var nextRequested = false

    @JvmField
    protected var prevPageListingId: String? = null

    @JvmField
    protected var nextPageListingId: String? = null

    @JvmField
    protected var subreddit: Subreddit? = null
    private var listingSelected: Listing? = null

    init {
        HoldTheNarwhal.getApplicationComponent().inject(this)
        this.mainView = main
        this.appRouter = appRouter
        this.linkCommentsRouter = linkCommentsRouter
        this.mediaGalleryRouter = mediaGalleryRouter
        this.videoPlayerRouter = videoPlayerRouter
        this.addCommentDialogRouter = addCommentDialogRouter
        this.reportViewRouter = reportViewRouter
        this.listingsView = view
    }

    open fun hasData(): Boolean {
        return listings.size != 0
    }

    open fun clearData() {
        listings.clear()
        listingsView.notifyDataSetChanged()
    }

    open fun refreshData() {
        prevPageListingId = null
        nextPageListingId = null
        val numItems = listings.size
        listings.clear()
        listingsView.notifyItemRangeRemoved(0, numItems)
        nextData
    }

    val previousData: Unit
        get() {
            if (!beforeRequested) {
                if (networkConnectivityManager!!.isConnectedToNetwork()) {
                    requestPreviousData()
                } else {
                    val message = context!!.getString(R.string.error_network_unavailable)
                    mainView.showToast(message)
                }
            }
        }
    val nextData: Unit
        get() {
            if (!nextRequested) {
                if (networkConnectivityManager!!.isConnectedToNetwork()) {
                    requestNextData()
                } else {
                    val message = context!!.getString(R.string.error_network_unavailable)
                    mainView.showToast(message)
                }
            }
        }

    protected abstract fun requestPreviousData()

    protected abstract fun requestNextData()

    override fun onFirstItemShown() {
        if (!beforeRequested && hasPreviousListings()) {
            Timber.d("Get PREVIOUS data")
            previousData
        }
    }

    override fun onLastItemShown() {
        if (!nextRequested && hasNextListings()) {
            Timber.d("Get NEXT data")
            nextData
        }
    }

    fun setData(data: List<Listing>) {
        listings.clear()
        listings.addAll(data)
    }

    open val numListings: Int
        get() = listings.size

    open fun getListingAt(position: Int): Listing? {
        return listings[position]
    }

    fun hasPreviousListings(): Boolean {
        return prevPageListingId != null
    }

    fun hasNextListings(): Boolean {
        return nextPageListingId != null
    }

    val showControversiality: Boolean
        get() = settingsManager!!.showControversiality

    protected fun onListingsLoaded(response: ListingResponse?, append: Boolean) {
        mainView.dismissSpinner()
        if (append) nextRequested = false else beforeRequested = false
        if (response == null) {
            mainView.showToast(context!!.getString(R.string.error_xxx))
            return
        }
        val data = response.data
        val listings = data.children
        Timber.i("Loaded %d listings", listings.size)
        if (append) {
            val lastIndex = this.listings.size - 1
            this.listings.addAll(listings)
            nextPageListingId = data.after
            listingsView.notifyItemRangeInserted(lastIndex + 1, listings.size)
        } else {
            this.listings.addAll(0, listings)
            prevPageListingId = if (listings.size == 0) null else listings[0].fullName
            listingsView.notifyItemRangeInserted(0, listings.size)
        }
    }

    protected fun checkNullResponse(listingResponse: ListingResponse): ObservableSource<ListingResponse> {
        return if (listingResponse.data.children == null) {
            prevPageListingId = null
            nextPageListingId = null
            Observable.error(NullPointerException("no links"))
        } else {
            Observable.just(listingResponse)
        }
    }

    fun onContextItemSelected(item: MenuItem): Boolean {
        // FIXME: We can now make a lot of these handlers private but some of them are still shared
        when (item.itemId) {
            R.id.action_link_reply -> {
                replyToLink(listingSelected as Link)
                return true
            }
            R.id.action_link_upvote -> {
                upvoteLink(listingSelected as Link)
                return true
            }
            R.id.action_link_downvote -> {
                downvoteLink(listingSelected as Link)
                return true
            }
            R.id.action_link_show_comments -> {
                showCommentsForLink(listingSelected as Link)
                return true
            }
            R.id.action_link_save -> {
                saveLink(listingSelected as Link)
                return true
            }
            R.id.action_link_unsave -> {
                unsaveLink(listingSelected as Link)
                return true
            }
            R.id.action_link_share -> {
                shareLink(listingSelected as Link)
                return true
            }
            R.id.action_link_view_subreddit -> {
                openLinkSubreddit(listingSelected as Link)
                return true
            }
            R.id.action_link_view_user_profile -> {
                openLinkUserProfile(listingSelected as Link)
                return true
            }
            R.id.action_link_open_in_browser -> {
                openLinkInBrowser(listingSelected as Link)
                return true
            }
            R.id.action_link_open_comments_in_browser -> {
                openCommentsInBrowser(listingSelected as Link)
                return true
            }
            R.id.action_link_hide -> {
                hideLink(listingSelected as Link)
                return true
            }
            R.id.action_link_unhide -> {
                unhideLink(listingSelected as Link)
                return true
            }
            R.id.action_link_report -> {
                listingSelected?.let { listing ->
                    reportViewRouter.openReportView(listing.fullName)
                }
                return true
            }
            R.id.action_comment_permalink -> {
                openCommentPermalink(listingSelected as Comment)
                return true
            }
            R.id.action_comment_parent -> {
                openCommentParent(listingSelected as Comment)
                return true
            }
            R.id.action_comment_reply -> {
                replyToComment(listingSelected as Comment)
                return true
            }
            R.id.action_comment_upvote -> {
                upvoteComment(listingSelected as Comment)
                return true
            }
            R.id.action_comment_downvote -> {
                downvoteComment(listingSelected as Comment)
                return true
            }
            R.id.action_comment_save -> {
                saveComment(listingSelected as Comment)
                return true
            }
            R.id.action_comment_unsave -> {
                unsaveComment(listingSelected as Comment)
                return true
            }
            R.id.action_comment_share -> {
                shareComment(listingSelected as Comment)
                return true
            }
            R.id.action_comment_view_user_profile -> {
                openCommentUserProfile(listingSelected as Comment)
                return true
            }
            R.id.action_comment_open_in_browser -> {
                openCommentInBrowser(listingSelected as Comment)
                return true
            }
            R.id.action_comment_report -> {
                reportComment(listingSelected as Comment)
                return true
            }
            R.id.action_message_show_permalink -> {
                showMessagePermalink(listingSelected as PrivateMessage)
                return true
            }
            R.id.action_message_report -> {
                reportMessage(listingSelected as PrivateMessage)
                return true
            }
            R.id.action_message_block_user -> {
                blockUser(listingSelected as PrivateMessage)
                return true
            }
            R.id.action_message_mark_read -> {
                markMessageRead(listingSelected as PrivateMessage)
                return true
            }
            R.id.action_message_mark_unread -> {
                markMessageUnread(listingSelected as PrivateMessage)
                return true
            }
            R.id.action_message_reply -> {
                replyToMessage(listingSelected as PrivateMessage)
                return true
            }
            else -> throw IllegalArgumentException("no action associated with item: ${item.title}")
        }
    }

    // Keep in sync with impl in LinkCommentsPresenter
    // TODO: Eventually consolidate routing so we don't have duplicate routing for Links
    open fun openLink(link: Link) {
        if (link.isSelf && link.subreddit != null) {
            linkCommentsRouter.showCommentsForLink(link.subreddit, link.id, null)
            return
        }

        // Determine correct routing for link
        if (link.isGallery) {
            val galleryItems = link.galleryItems
            mediaGalleryRouter.openLinkGallery(galleryItems)
            return
        }
        val media = link.media
        if (media != null) {
            val redditVideo = media.redditVideo
            if (redditVideo != null) {
                videoPlayerRouter.openRedditVideo(redditVideo)
                return
            }
        }
        val linkUrl = link.url
        if (linkUrl != null) {
            appRouter.openUrl(linkUrl)
        }
    }

    fun showCommentsForLink(link: Link) {
        linkCommentsRouter.showCommentsForLink(link.subreddit, link.id, null)
    }

    open fun replyToLink(link: Link) {
        addCommentDialogRouter.openReplyDialog(link.fullName)
    }

    fun upvoteLink(link: Link) {
        val dir = if (link.liked == null || !link.liked!!) 1 else 0
        vote(link, link.id, dir)
    }

    fun downvoteLink(link: Link) {
        val dir = if (link.liked == null || link.liked!!) -1 else 0
        vote(link, link.id, dir)
    }

    fun saveLink(link: Link) {
        if (!redditService!!.isUserAuthorized) {
            mainView.showToast(context!!.getString(R.string.user_required))
            return
        }
        save(link, true)
    }

    fun unsaveLink(link: Link) {
        if (!redditService!!.isUserAuthorized) {
            mainView.showToast(context!!.getString(R.string.user_required))
            return
        }
        save(link, false)
    }

    fun shareLink(link: Link) {
        appRouter.openShareView(link)
    }

    fun openLinkSubreddit(link: Link) {
        val subreddit = link.subreddit
        if (subreddit != null) {
            appRouter.showSubreddit(subreddit, null, null)
        }
    }

    fun openLinkUserProfile(link: Link) {
        val author = link.author
        if (author != null) {
            appRouter.showUserProfile(author, null, null)
        }
    }

    fun openLinkInBrowser(link: Link) {
        appRouter.openLinkInBrowser(link)
    }

    fun openCommentsInBrowser(link: Link) {
        appRouter.openLinkCommentsInBrowser(link)
    }

    fun hideLink(link: Link) {
        if (!redditService!!.isUserAuthorized) {
            mainView.showToast(context!!.getString(R.string.user_required))
            return
        }
        hide(link, true)
    }

    fun unhideLink(link: Link) {
        if (!redditService!!.isUserAuthorized) {
            mainView.showToast(context!!.getString(R.string.user_required))
            return
        }
        hide(link, false)
    }

    fun reportLink(link: Link) {
        if (!redditService!!.isUserAuthorized) {
            mainView.showToast(context!!.getString(R.string.user_required))
        } else {
            reportViewRouter.openReportView(link.fullName)
        }
    }

    fun showCommentThread(
        subreddit: String, linkId: String, commentId: String
    ) {
        linkCommentsRouter.showCommentsForLink(subreddit, linkId, commentId)
    }

    open fun getMoreComments(comment: CommentStub) {
        // Comment stubs cannot appear in a listing view
    }

    fun openCommentPermalink(comment: Comment) {
        showCommentThread(comment.subreddit, comment.linkId, comment.id)
    }

    fun openCommentParent(comment: Comment) {
        showCommentThread(comment.subreddit, comment.linkId, comment.parentId)
    }

    open fun replyToComment(comment: Comment) {
        if (comment.archived) {
            mainView.showToast(context!!.getString(R.string.listing_archived))
        } else {
            addCommentDialogRouter.openReplyDialog(comment.fullName)
        }
    }

    fun upvoteComment(comment: Comment) {
        val liked = comment.liked
        val dir = if (liked == null || !liked) 1 else 0
        vote(comment, comment.id, dir)
    }

    fun downvoteComment(comment: Comment) {
        val liked = comment.liked
        val dir = if (liked == null || liked) -1 else 0
        vote(comment, comment.id, dir)
    }

    fun saveComment(comment: Comment) {
        if (!redditService!!.isUserAuthorized) {
            mainView.showToast(context!!.getString(R.string.user_required))
            return
        }
        save(comment, true)
    }

    fun unsaveComment(comment: Comment) {
        if (!redditService!!.isUserAuthorized) {
            mainView.showToast(context!!.getString(R.string.user_required))
            return
        }
        save(comment, false)
    }

    fun shareComment(comment: Comment) {
        appRouter.openShareView(comment)
    }

    fun openCommentUserProfile(comment: Comment) {
        appRouter.showUserProfile(comment.author, null, null)
    }

    fun openCommentInBrowser(comment: Comment) {
        appRouter.openCommentInBrowser(comment)
    }

    fun reportComment(comment: Comment) {
        if (comment.archived) {
            mainView.showToast(context!!.getString(R.string.listing_archived))
        } else if (!redditService!!.isUserAuthorized) {
            mainView.showToast(context!!.getString(R.string.user_required))
        } else {
            reportViewRouter.openReportView(comment.fullName)
        }
    }

    open fun openCommentLink(comment: Comment) {
        linkCommentsRouter.showCommentsForLink(comment.subreddit, comment.linkId, null)
    }

    val authorizedUser: UserIdentity
        get() = identityManager!!.userIdentity

    fun onSortChanged() {
        refreshData()
    }

    private fun vote(votable: Votable, votableId: String, direction: Int) {
        if (votable.archived) {
            mainView.showToast(context!!.getString(R.string.listing_archived))
        } else if (!redditService!!.isUserAuthorized) {
            mainView.showToast(context!!.getString(R.string.user_required))
        } else {
            redditService!!.vote(votable.kind + "_" + votableId, direction)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        votable.applyVote(direction)
                        listingsView.notifyItemChanged(getIndexOf(votable as Listing))
                    }
                ) { error: Throwable? ->
                    if (error is IOException) {
                        val message = context!!.getString(R.string.error_network_unavailable)
                        mainView.showError(message)
                    } else {
                        Timber.w(error, "Error voting on listing")
                        val message = context!!.getString(R.string.vote_failed)
                        mainView.showError(message)
                    }
                }
        }
    }

    /**
     * This is overridden in link comments view which has headers
     */
    protected open fun getIndexOf(listing: Listing): Int {
        return listings.indexOf(listing)
    }

    private fun save(savable: Savable, toSave: Boolean) {
        redditService!!.save(savable.fullName, null, toSave)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    savable.isSaved = toSave
                    listingsView.notifyItemChanged(getIndexOf(savable as Listing))
                }
            ) { error: Throwable? ->
                if (error is IOException) {
                    val message = context!!.getString(R.string.error_network_unavailable)
                    mainView.showError(message)
                } else {
                    Timber.w(error, "Error saving listing")
                    val message = context!!.getString(R.string.save_failed)
                    mainView.showError(message)
                }
            }
    }

    private fun hide(hideable: Hideable, toHide: Boolean) {
        redditService!!.hide(hideable.fullName, toHide)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    val pos = getIndexOf(hideable as Listing)
                    listings.removeAt(pos)
                    listingsView.notifyItemRemoved(pos)
                }
            ) { error: Throwable? ->
                if (error is IOException) {
                    val message = context!!.getString(R.string.error_network_unavailable)
                    mainView.showError(message)
                } else {
                    Timber.w(error, "Error hiding listing")
                    val message = context!!.getString(R.string.hide_failed)
                    mainView.showError(message)
                }
            }
    }

    fun shouldShowNsfwTag(): Boolean {
        val isNsfwSubreddit = subreddit != null && subreddit!!.isOver18
        val hideNsfwInSettings = settingsManager!!.noProfanity || settingsManager!!.labelNsfw
        val userOver18 = settingsManager!!.over18
        return !userOver18 || !isNsfwSubreddit && hideNsfwInSettings
    }

    val thumbnailMode: ThumbnailMode
        get() = if (settingsManager!!.over18) {
            if (subreddit != null && subreddit!!.isOver18) {
                ThumbnailMode.FULL
            } else {
                if (settingsManager!!.noProfanity) {
                    ThumbnailMode.VARIANT
                } else {
                    if (settingsManager!!.labelNsfw) {
                        ThumbnailMode.VARIANT
                    } else {
                        ThumbnailMode.FULL
                    }
                }
            }
        } else {
            ThumbnailMode.NO_THUMBNAIL
        }
    val userIdentity: UserIdentity
        get() = identityManager!!.userIdentity

    fun replyToMessage(message: PrivateMessage) {
        mainView.showToast(context!!.getString(R.string.implementation_pending))
    }

    fun markMessageRead(pm: PrivateMessage) {
        val fullname = pm.fullName
        redditService!!.markMessagesRead(fullname)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    pm.markUnread(false)
                    listingsView.notifyItemChanged(getIndexOf(pm))
                }
            ) { error: Throwable? ->
                if (error is IOException) {
                    val message = context!!.getString(R.string.error_network_unavailable)
                    mainView.showError(message)
                } else {
                    Timber.w(error, "Error marking message read")
                    val errorMessage = context!!.getString(R.string.error_xxx)
                    mainView.showError(errorMessage)
                }
            }
    }

    fun markMessageUnread(pm: PrivateMessage) {
        val fullname = pm.fullName
        redditService!!.markMessagesUnread(fullname)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    pm.markUnread(true)
                    listingsView.notifyItemChanged(getIndexOf(pm))
                }
            ) { error: Throwable? ->
                if (error is IOException) {
                    val message = context!!.getString(R.string.error_network_unavailable)
                    mainView.showError(message)
                } else {
                    Timber.w(error, "Error marking message unread")
                    val errorMessage = context!!.getString(R.string.error_xxx)
                    mainView.showError(errorMessage)
                }
            }
    }

    fun showMessagePermalink(message: PrivateMessage) {
        val listingResponse = message.replies
        val messages: MutableList<PrivateMessage> = ArrayList()
        if (listingResponse != null) {
            for (item in listingResponse.data.children) {
                messages.add(item as PrivateMessage)
            }
        }
        messages.add(0, message)
        appRouter.showInboxMessages(messages)
    }

    fun reportMessage(message: PrivateMessage) {
        reportViewRouter.openReportView(message.fullName)
    }

    fun blockUser(message: PrivateMessage) {
        mainView.showToast(context!!.getString(R.string.implementation_pending))
    }

    fun onContextMenuShownForLink(link: Link) {
        listingSelected = link
    }

    fun onContextMenuShownForComment(comment: Comment) {
        listingSelected = comment
    }
}
