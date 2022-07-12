package com.ddiehl.android.htn.listings.comments

import androidx.fragment.app.FragmentActivity
import javax.inject.Inject

class LinkCommentsRouter @Inject constructor(
    private val activity: FragmentActivity,
) {

    fun showCommentsForLink(
        subreddit: String?,
        linkId: String?,
        commentId: String?,
    ) {
        val intent = LinkCommentsActivity.getIntent(activity, subreddit, linkId, commentId)
        activity.startActivity(intent)
    }
}
