package com.ddiehl.android.simpleredditreader.model.auth;

import com.ddiehl.android.simpleredditreader.events.requests.LoadCommentThreadEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadCommentsEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadLinksEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadMoreChildrenEvent;
import com.ddiehl.android.simpleredditreader.events.requests.VoteEvent;


public interface RedditService {
    public void onLoadLinks(LoadLinksEvent event);
    public void onLoadComments(LoadCommentsEvent event);
    public void onLoadMoreChildren(LoadMoreChildrenEvent event);
    public void onLoadCommentThread(LoadCommentThreadEvent event);
    public void onVote(VoteEvent event);
}
