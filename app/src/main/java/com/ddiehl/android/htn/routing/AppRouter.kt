package com.ddiehl.android.htn.routing

import androidx.fragment.app.FragmentActivity
import com.ddiehl.android.htn.listings.inbox.InboxActivity
import com.ddiehl.android.htn.listings.inbox.PrivateMessageActivity
import com.ddiehl.android.htn.listings.profile.UserProfileActivity
import com.ddiehl.android.htn.listings.subreddit.SubredditActivity
import com.ddiehl.android.htn.navigation.SubredditNavigationDialog
import com.ddiehl.android.htn.settings.SettingsActivity
import com.ddiehl.android.htn.subscriptions.SubscriptionManagerActivity
import com.google.gson.Gson
import rxreddit.model.PrivateMessage
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
}
