package com.ddiehl.android.htn.listings.comments

import android.view.ContextMenu
import androidx.fragment.app.FragmentActivity
import com.ddiehl.android.htn.R
import rxreddit.model.Comment

/**
 * Contains utilities related to inflating a [ContextMenu] for a [Comment] model.
 */
object CommentMenuHelper {

    @JvmStatic
    fun showCommentContextMenu(activity: FragmentActivity, menu: ContextMenu, comment: Comment) {
        activity.menuInflater.inflate(R.menu.comment_context, menu)

        // Build title for menu
        val score = if (comment.score == null) {
            activity.getString(R.string.hidden_score_placeholder)
        } else {
            comment.score!!.toString()
        }
        val title = activity.getString(R.string.menu_action_comment).format(comment.author, score)
        menu.setHeaderTitle(title)

        // Set username for listing in the user profile menu item
        val username = activity.getString(R.string.action_view_user_profile).format(comment.author)
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
}
