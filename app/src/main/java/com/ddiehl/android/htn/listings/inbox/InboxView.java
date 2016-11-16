package com.ddiehl.android.htn.listings.inbox;

import android.support.annotation.NonNull;

import com.ddiehl.android.htn.view.CommentView;
import com.ddiehl.android.htn.view.LinkView;
import com.ddiehl.android.htn.view.ListingsView;
import com.ddiehl.android.htn.view.PrivateMessageView;

public interface InboxView extends ListingsView, LinkView, CommentView, PrivateMessageView {

    void selectTab(@NonNull String show);

    String getShow();

    // Call this when the user no longer has permission to view inbox
    void finish();
}
