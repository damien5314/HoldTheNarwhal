package com.ddiehl.android.htn.listings.inbox;

import android.support.annotation.NonNull;

import com.ddiehl.android.htn.listings.ListingsView;
import com.ddiehl.android.htn.listings.comments.CommentView;
import com.ddiehl.android.htn.listings.links.LinkView;

public interface InboxView extends ListingsView, LinkView, CommentView, PrivateMessageView {

    void selectTab(@NonNull String show);

    String getShow();

    // Call this when the user no longer has permission to view inbox
    void finish();
}
