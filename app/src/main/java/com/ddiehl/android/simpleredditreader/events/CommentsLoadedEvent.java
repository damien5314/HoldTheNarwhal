package com.ddiehl.android.simpleredditreader.events;

import com.ddiehl.android.simpleredditreader.redditapi.comments.CommentsResponse;
import com.ddiehl.android.simpleredditreader.redditapi.comments.RedditComment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Damien on 1/19/2015.
 */
public class CommentsLoadedEvent {
    List<RedditComment> mComments;

    public CommentsLoadedEvent(List<CommentsResponse> response) {
        mComments = new ArrayList<>();

        // Listing is response.get(0), comments are response.get(1)
        List<RedditComment> comments = response.get(1).getData().getComments();

        for (RedditComment comment : comments) {
            mComments.add(comment);
        }
    }

    public List<RedditComment> getComments() {
        return mComments;
    }
}