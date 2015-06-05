package com.ddiehl.android.simpleredditreader.view;

public interface LinksView extends LinkView {

    void linksUpdated();
    void linkUpdatedAt(int position);
    void linkRemovedAt(int position);

}
