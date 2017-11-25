package com.ddiehl.android.htn.listings.comments

import com.ddiehl.android.htn.listings.ListingsView
import com.ddiehl.android.htn.listings.links.LinkView

interface LinkCommentsView : ListingsView, LinkView, CommentView {

    val subreddit: String

    val articleId: String

    val commentId: String?

    var sort: String

    fun refreshOptionsMenu()
}
