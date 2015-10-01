package com.ddiehl.android.htn.events.responses;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ddiehl.reddit.listings.CommentStub;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.MoreChildrenResponse;

import java.util.List;

public class MoreChildrenLoadedEvent extends FailableEvent {
    private CommentStub mParentStub;
    private List<Listing> mComments;

    public MoreChildrenLoadedEvent(@NonNull CommentStub parentStub, @NonNull MoreChildrenResponse moreChildrenResponse) {
        mParentStub = parentStub;
        mComments = moreChildrenResponse.getChildComments();
    }

    public MoreChildrenLoadedEvent(@NonNull Throwable e) {
        super(e);
    }

    @Nullable
    public CommentStub getParentStub() {
        return mParentStub;
    }

    @Nullable
    public List<Listing> getComments() {
        return mComments;
    }
}
