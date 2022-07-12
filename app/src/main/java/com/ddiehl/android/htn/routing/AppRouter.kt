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
import com.ddiehl.android.htn.utils.getColorFromAttr
import com.google.gson.Gson
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

    fun showSubreddit(subreddit: String, sort: String?, timespan: String?) {
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
        if (intent.resolveActivity(activity.packageManager) != null) {
            Timber.e("No Activity found that can handle custom tabs Intent")
            activity.startActivity(intent)
        } else {
            val intent = WebViewActivity.getIntent(activity, url)
            activity.startActivity(intent)
        }
    }
}
