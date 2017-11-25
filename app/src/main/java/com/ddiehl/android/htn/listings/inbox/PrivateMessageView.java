package com.ddiehl.android.htn.listings.inbox;

import android.view.ContextMenu;
import android.view.View;

import org.jetbrains.annotations.NotNull;

import rxreddit.model.PrivateMessage;

public interface PrivateMessageView {

    void showMessageContextMenu(ContextMenu menu, View view, PrivateMessage privateMessage);

    void openReportView(@NotNull PrivateMessage message);
}
