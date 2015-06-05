package com.ddiehl.android.simpleredditreader.io;

import com.ddiehl.android.simpleredditreader.events.requests.HideEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadLinkCommentsEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadLinksEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadMoreChildrenEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadUserOverviewEvent;
import com.ddiehl.android.simpleredditreader.events.requests.ReportEvent;
import com.ddiehl.android.simpleredditreader.events.requests.SaveEvent;
import com.ddiehl.android.simpleredditreader.events.requests.VoteEvent;


public interface RedditService {

    String USER_AGENT = "android:com.ddiehl.android.simpleredditreader:v0.1 (by /u/damien5314)";
    String ENDPOINT_NORMAL = "https://www.reddit.com";
    String ENDPOINT_AUTHORIZED = "https://oauth.reddit.com";

    void onLoadLinks(LoadLinksEvent event);
    void onLoadLinkComments(LoadLinkCommentsEvent event);
    void onLoadMoreChildren(LoadMoreChildrenEvent event);
    void onLoadUserOverview(LoadUserOverviewEvent event);

    void onVote(VoteEvent event);
    void onSave(SaveEvent event);
    void onHide(HideEvent event);
    void onReport(ReportEvent event);
}
