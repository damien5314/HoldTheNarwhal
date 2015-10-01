package com.ddiehl.android.htn.events.requests;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ddiehl.reddit.listings.CommentStub;
import com.ddiehl.reddit.listings.Link;

import java.util.List;

public class LoadMoreChildrenEvent {
    private Link mLink;
    private CommentStub mMoreComments;
    private List<String> mChildren;
    private String mSort;

    public LoadMoreChildrenEvent(@NonNull Link link, @NonNull CommentStub moreComments,
                                 @NonNull List<String> children, @Nullable String sort) {
        mLink = link;
        mMoreComments = moreComments;
        mChildren = children;
        mSort = sort;
    }

    @NonNull
    public Link getLink() {
        return mLink;
    }

    @NonNull
    public CommentStub getParentCommentStub() {
        return mMoreComments;
    }

    @NonNull
    public List<String> getChildren() {
        return mChildren;
    }

    @Nullable
    public String getSort() {
        return mSort;
    }
}
