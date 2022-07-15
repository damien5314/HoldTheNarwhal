package com.ddiehl.android.htn.listings.inbox;

import com.ddiehl.android.htn.listings.ListingsView;
import com.ddiehl.android.htn.listings.comments.CommentView;

import org.jetbrains.annotations.NotNull;

public interface InboxView extends ListingsView, CommentView, PrivateMessageView {

    void selectTab(@NotNull String show);

    String getShow();

    // Call this when the user no longer has permission to view inbox
    void finish();
}
