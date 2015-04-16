package com.ddiehl.android.simpleredditreader.model.auth;

import com.ddiehl.android.simpleredditreader.events.LoadCommentThreadEvent;
import com.ddiehl.android.simpleredditreader.events.LoadCommentsEvent;
import com.ddiehl.android.simpleredditreader.events.LoadLinksEvent;
import com.ddiehl.android.simpleredditreader.events.LoadMoreChildrenEvent;
import com.ddiehl.android.simpleredditreader.events.VoteEvent;


public interface IRedditService {
    public void onLoadLinks(LoadLinksEvent event);
    public void onLoadComments(LoadCommentsEvent event);
    public void onLoadMoreChildren(LoadMoreChildrenEvent event);
    public void onLoadCommentThread(LoadCommentThreadEvent event);
    public void onVote(VoteEvent event);
}
