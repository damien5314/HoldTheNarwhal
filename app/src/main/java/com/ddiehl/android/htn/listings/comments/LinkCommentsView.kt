package com.ddiehl.android.htn.listings.comments

import com.ddiehl.android.htn.listings.ListingsView

interface LinkCommentsView : ListingsView, CommentView {

    val subreddit: String

    val articleId: String

    val commentId: String?

    var sort: String

    fun refreshOptionsMenu()
}
