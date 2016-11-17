package com.ddiehl.android.htn.listings.comments;

import com.ddiehl.android.htn.listings.ListingsView;
import com.ddiehl.android.htn.listings.links.LinkView;

public interface LinkCommentsView extends ListingsView, LinkView, CommentView {

    String getCommentId();

    String getArticleId();

    String getSubreddit();

    String getSort();

    void refreshOptionsMenu();
}
