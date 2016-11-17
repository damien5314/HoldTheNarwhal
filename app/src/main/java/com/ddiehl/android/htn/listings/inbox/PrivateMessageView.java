package com.ddiehl.android.htn.listings.inbox;

import android.view.ContextMenu;
import android.view.View;

import rxreddit.model.PrivateMessage;

public interface PrivateMessageView {

    void showMessageContextMenu(ContextMenu menu, View view, PrivateMessage privateMessage);
}
