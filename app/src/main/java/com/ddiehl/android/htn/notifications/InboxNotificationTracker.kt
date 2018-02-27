package com.ddiehl.android.htn.notifications

import android.content.Context

private const val PREFS_KEY = "inbox_notification_tracker"
private const val KEY_LAST_MESSAGE = "last_message"

/**
 * Class which tracks the last new inbox message which triggered
 * a notification to the user.
 */
class InboxNotificationTracker(applicationContext: Context) {

    private val sharedPreferences =
            applicationContext.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

    var lastMessageId: String?
        get() = sharedPreferences.getString(KEY_LAST_MESSAGE, null)
        set(value) = sharedPreferences.edit().putString(KEY_LAST_MESSAGE, value).apply()
}
