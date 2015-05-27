package com.ddiehl.android.simpleredditreader.view;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;

import com.ddiehl.reddit.listings.RedditLink;

public interface LinksView extends BaseView {

    RecyclerView.Adapter<RecyclerView.ViewHolder> getListAdapter();
    void openWebViewForLink(RedditLink link);
    void showCommentsForLink(RedditLink link);
    void showLinkContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, RedditLink link);

    void openShareView(RedditLink link);
    void openLinkInBrowser(RedditLink link);
    void openCommentsInBrowser(RedditLink link);

}
