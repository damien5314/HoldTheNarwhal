package com.ddiehl.android.simpleredditreader.presenter;

import android.view.ContextMenu;
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

    void showLinkContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, RedditLink link);

    void openLink(RedditLink link);
    void showCommentsForLink(RedditLink link);
    void showCommentsForLink();
    void upvote();
    void downvote();
    void saveLink();
    void unsaveLink();
    void shareLink();
    void openLinkInBrowser();
    void openCommentsInBrowser();
    void hideLink();
    void unhideLink();
    void reportLink();

}
