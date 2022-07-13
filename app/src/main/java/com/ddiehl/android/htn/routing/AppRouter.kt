package com.ddiehl.android.htn.routing

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.ddiehl.android.htn.R
import com.ddiehl.android.htn.listings.inbox.InboxActivity
import com.ddiehl.android.htn.listings.inbox.PrivateMessageActivity
import com.ddiehl.android.htn.listings.profile.UserProfileActivity
import com.ddiehl.android.htn.listings.subreddit.SubredditActivity
import com.ddiehl.android.htn.navigation.SubredditNavigationDialog
import com.ddiehl.android.htn.navigation.WebViewActivity
import com.ddiehl.android.htn.settings.SettingsActivity
import com.ddiehl.android.htn.subscriptions.SubscriptionManagerActivity
import com.ddiehl.android.htn.utils.AndroidUtils
import com.ddiehl.android.htn.utils.getColorFromAttr
import com.google.gson.Gson
import rxreddit.model.Comment
import rxreddit.model.Link
import rxreddit.model.PrivateMessage
import timber.log.Timber
import javax.inject.Inject

/**
 * Router that links to just about everything.
 *
 * TODO: Break this into fragment-specific routers so we can modularize later.
 */
class AppRouter @Inject constructor(
    private val activity: FragmentActivity,
    private val gson: Gson,
) {

    companion object {
        private const val EXTRA_CUSTOM_TABS_SESSION = "android.support.customtabs.extra.SESSION"
        private const val EXTRA_CUSTOM_TABS_TOOLBAR_COLOR = "android.support.customtabs.extra.TOOLBAR_COLOR"
        private const val LINK_BASE_URL = "https://www.reddit.com"
    }

    fun showInbox(show: String? = null) {
        val intent = InboxActivity.getIntent(activity, show)
        activity.startActivity(intent)
    }

    fun showInboxMessages(messages: List<PrivateMessage>) {
        val messageJson = gson.toJson(messages)
        val intent = PrivateMessageActivity.getIntent(activity, messageJson)
        activity.startActivity(intent)
    }

    fun showSettings() {
        val intent = SettingsActivity.getIntent(activity)
        activity.startActivity(intent)
    }

    fun showFrontPage(sort: String?, timespan: String?) {
        val intent = SubredditActivity.getIntent(activity, null, sort, timespan)
        activity.startActivity(intent)
    }

    fun openShareView(link: Link) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, LINK_BASE_URL + link.permalink)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        activity.startActivity(intent)
    }

    fun openShareView(comment: Comment) {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_TEXT, comment.url)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(i)
    }

    fun showSubreddit(subreddit: String, sort: String? = null, timespan: String? = null) {
        val intent = SubredditActivity.getIntent(activity, subreddit, sort, timespan)
        activity.startActivity(intent)
    }

    fun showSubredditNavigationView() {
        val dialog = SubredditNavigationDialog()
        dialog.show(activity.supportFragmentManager, SubredditNavigationDialog.TAG)
    }

    fun showUserProfile(
        username: String,
        show: String?,
        sort: String?,
    ) {
        val intent = UserProfileActivity.getIntent(activity, username, show, sort)
        activity.startActivity(intent)
    }

    fun showUserSubreddits() {
        val intent = SubscriptionManagerActivity.getIntent(activity)
        activity.startActivity(intent)
    }

    fun openUrl(url: String) {
        // If so, present URL in custom tabs instead of WebView
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        val extras = Bundle()
        // Pass IBinder instead of null for a custom tabs session
        extras.putBinder(EXTRA_CUSTOM_TABS_SESSION, null)
        val toolbarColor = getColorFromAttr(activity, R.attr.colorPrimary)
        extras.putInt(EXTRA_CUSTOM_TABS_TOOLBAR_COLOR, toolbarColor)
        intent.putExtras(extras)

        // Check if Activity exists to handle the Intent
        val activityStarted = AndroidUtils.safeStartActivity(activity, intent)
        if (!activityStarted) {
            Timber.e("No Activity found that can handle custom tabs Intent")
            val intent = WebViewActivity.getIntent(activity, url)
            activity.startActivity(intent)
        }
    }

    fun openLinkInBrowser(link: Link) {
        val uri = Uri.parse(link.url)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        AndroidUtils.safeStartActivity(activity, intent)
    }

    fun openLinkCommentsInBrowser(link: Link) {
        val uri = Uri.parse(LINK_BASE_URL + link.permalink)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        AndroidUtils.safeStartActivity(activity, intent)
    }

    fun openCommentInBrowser(comment: Comment) {
        val uri = Uri.parse(comment.url)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        AndroidUtils.safeStartActivity(activity, intent)
    }
}
