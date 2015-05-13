package com.ddiehl.android.simpleredditreader.model.auth;

import com.ddiehl.android.simpleredditreader.events.requests.LoadCommentThreadEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadCommentsEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadLinksEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadMoreChildrenEvent;
import com.ddiehl.android.simpleredditreader.events.requests.SaveEvent;
import com.ddiehl.android.simpleredditreader.events.requests.VoteEvent;


public interface RedditService {
    void onLoadLinks(LoadLinksEvent event);
    void onLoadComments(LoadCommentsEvent event);
    void onLoadMoreChildren(LoadMoreChildrenEvent event);
    void onLoadCommentThread(LoadCommentThreadEvent event);
    void onVote(VoteEvent event);
    void onSave(SaveEvent event);
}
