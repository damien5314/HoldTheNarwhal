package com.ddiehl.android.htn.listings.links;

import android.view.ContextMenu;
import android.view.View;

import org.jetbrains.annotations.NotNull;

import rxreddit.model.Link;

public interface LinkView {

    void showLinkContextMenu(ContextMenu menu, View view, Link link);

    void openReportView(@NotNull Link link);
}
