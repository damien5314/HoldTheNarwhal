package com.ddiehl.android.htn.listings.comments;

import com.ddiehl.android.htn.listings.CommentView;
import com.ddiehl.android.htn.listings.LinkView;
import com.ddiehl.android.htn.listings.ListingsView;

public interface LinkCommentsView extends ListingsView, LinkView, CommentView {

    String getCommentId();

    String getArticleId();

    String getSubreddit();

    String getSort();

    void refreshOptionsMenu();
}
