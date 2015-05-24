package com.ddiehl.android.simpleredditreader.presenter;

import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;

import com.ddiehl.reddit.listings.RedditLink;

public interface LinksPresenter {

    void getLinks();
    void getMoreLinks();
    RedditLink getLink(int position);
    int getNumLinks();
    String getSubreddit();
    String getSort();
    String getTimeSpan();
    void updateTitle();
    void updateSubreddit(String subreddit);
    void updateSort(String sort);
    void updateTimeSpan(String timespan);

    void showContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, RedditLink link);
    boolean onContextItemSelected(MenuItem item);

    void openLink(RedditLink link);
    void openCommentsForLink(RedditLink link);

    void hide(RedditLink link, boolean toHide);
    boolean isHidden(RedditLink link);
}
