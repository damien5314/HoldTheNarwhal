package com.ddiehl.android.htn.listings.subreddit;

import com.ddiehl.android.htn.listings.LinkView;
import com.ddiehl.android.htn.listings.ListingsView;

public interface SubredditView extends ListingsView, LinkView {

    String getSubreddit();

    String getSort();

    String getTimespan();

    void showNsfwWarningDialog();

    void onRandomSubredditLoaded(String randomSubreddit);

    void loadHeaderImage();
}
