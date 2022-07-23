package com.ddiehl.android.htn.listings.links

import android.view.ContextMenu
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.ddiehl.android.htn.R
import rxreddit.model.Link

object LinkViewHolderContextMenuUtil {

    // FIXME: This is just for convenience before BaseLinkViewHolder is converted to Kotlin
    @JvmStatic
    fun showLinkContextMenu(menu: ContextMenu, view: View, link: Link) {
        (view.context as? FragmentActivity)?.menuInflater?.inflate(R.menu.link_context, menu)

        // Build title for menu
        val score = if (link.score == null) {
            view.context.getString(R.string.hidden_score_placeholder)
        } else {
            link.score.toString()
        }
        val title = view.context.getString(R.string.menu_action_link).format(link.title, score)
        menu.setHeaderTitle(title)

        // Set state of hide/unhide
        menu.findItem(R.id.action_link_hide).isVisible = !link.hidden
        menu.findItem(R.id.action_link_unhide).isVisible = link.hidden

        // Set subreddit for link in the view subreddit menu item
        val subreddit = view.context.getString(R.string.action_view_subreddit).format(link.subreddit)
        menu.findItem(R.id.action_link_view_subreddit).title = subreddit

        // Set username for link in the view user profile menu item
        val username = view.context.getString(R.string.action_view_user_profile).format(link.author)
        menu.findItem(R.id.action_link_view_user_profile).title = username

        // Hide user profile for posts by deleted users
        if ("[deleted]".equals(link.author, ignoreCase = true)) {
            menu.findItem(R.id.action_link_view_user_profile).isVisible = false
        }

        menu.findItem(R.id.action_link_reply).isVisible = false
        menu.findItem(R.id.action_link_save).isVisible = !link.isSaved
        menu.findItem(R.id.action_link_unsave).isVisible = link.isSaved
    }
}
