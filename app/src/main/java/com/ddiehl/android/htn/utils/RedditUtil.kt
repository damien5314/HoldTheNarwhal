package com.ddiehl.android.htn.utils

import rxreddit.model.Comment
import rxreddit.model.CommentStub
import rxreddit.model.Listing

/**
 * Flattens list of comments, marking each comment with depth
 */
fun flattenCommentList(commentList: MutableList<Listing>) {
    for (i in 0 until commentList.size) {
        val listing = commentList[i]
        if (listing is Comment) {
            val repliesListing = listing.replies
            if (repliesListing != null) {
                val replies = repliesListing.data.children
                flattenCommentList(replies)
            }
            listing.depth = listing.depth + 1 // Increase depth by 1
            if (listing.replies != null) {
                // Add all of the replies to commentList
                commentList.addAll(i + 1, listing.replies.data.children)
                listing.replies = null // Remove replies for comment
            }
        } else { // Listing is a CommentStub
            val moreComments = listing as CommentStub
            moreComments.depth = moreComments.depth + 1 // Increase depth by 1
        }
    }
}