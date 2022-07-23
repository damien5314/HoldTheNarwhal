package com.ddiehl.android.htn.listings.inbox

import android.view.ContextMenu
import androidx.fragment.app.FragmentActivity
import com.ddiehl.android.htn.R
import rxreddit.model.PrivateMessage

/**
 * Contains utilities related to inflating a [ContextMenu] for a private messages.
 */
object InboxMenuHelper {

    fun showMessageContextMenu(activity: FragmentActivity, menu: ContextMenu, message: PrivateMessage) {
        activity.menuInflater.inflate(R.menu.message_context, menu)

        // Hide ride/unread option based on state
        menu.findItem(R.id.action_message_mark_read).isVisible = message.isUnread!!
        menu.findItem(R.id.action_message_mark_unread).isVisible = !message.isUnread
    }
}
