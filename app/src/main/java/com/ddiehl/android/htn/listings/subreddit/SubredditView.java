package com.ddiehl.android.htn.listings.subreddit;

import com.ddiehl.android.htn.listings.ListingsView;
import com.ddiehl.android.htn.listings.links.LinkView;

public interface SubredditView extends ListingsView, LinkView {

    String getSubreddit();

    String getSort();

    String getTimespan();

    void showNsfwWarningDialog();

    void onRandomSubredditLoaded(String randomSubreddit);

    void loadHeaderImage();

    void refreshOptionsMenu();
}
