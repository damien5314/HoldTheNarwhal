/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.model;

import com.ddiehl.reddit.listings.AbsComment;
import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.Listing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommentBankList implements CommentBank {
    private static final String TAG = CommentBankList.class.getSimpleName();

    private List<Listing> mData;
    private List<Listing> mVisibleData;

    public CommentBankList() {
        mData = new ArrayList<>();
        mVisibleData = new ArrayList<>();
    }

    @Override
    public boolean addAll(Collection<Listing> collection) {
        boolean result = mData.addAll(collection);
        syncVisibleData();
        return result;
    }

    @Override
    public boolean addAll(int index, Collection<Listing> collection) {
        boolean result = mData.addAll(index, collection);
        syncVisibleData();
        return result;
    }

    @Override
    public int indexOf(AbsComment o) {
        return mData.indexOf(o);
    }

    @Override
    public int visibleIndexOf(AbsComment obj) {
        return mVisibleData.indexOf(obj);
    }

    @Override
    public AbsComment get(int position) {
        return (AbsComment) mData.get(position);
    }

    @Override
    public int size() {
        return mData.size();
    }

    @Override
    public AbsComment remove(int position) {
        AbsComment result = (AbsComment) mData.remove(position);
        syncVisibleData();
        return result;
    }

    @Override
    public boolean remove(AbsComment comment) {
        boolean removed = mData.remove(comment);
        syncVisibleData();
        return removed;
    }

    @Override
    public void clear() {
        mData.clear();
        syncVisibleData();
    }

    @Override
    public void setData(List<Listing> data) {
        clear();
        addAll(data);
        syncVisibleData();
    }

    @Override
    public boolean isVisible(int position) {
        return ((AbsComment) mData.get(position)).isVisible();
    }

    @Override
    public int getNumVisible() {
        return mVisibleData.size();
    }

    @Override
    public AbsComment getVisibleComment(int position) {
        return (AbsComment) mVisibleData.get(position);
    }

    private void syncVisibleData() {
        mVisibleData.clear();
        for (Listing comment : mData) {
            if (((AbsComment) comment).isVisible()) {
                mVisibleData.add(comment);
            }
        }
    }

    @Override
    public void toggleThreadVisible(AbsComment comment) {
        setThreadVisible(indexOf(comment), comment.isCollapsed());
    }

    private void setThreadVisible(int position, boolean visible) {
        int totalItemCount = mData.size();
        Comment parentComment = (Comment) mData.get(position);
        int parentCommentDepth = parentComment.getDepth();
        parentComment.setCollapsed(!visible);
        ArrayList<Integer> collapsedCommentLevels = new ArrayList<>();
        if (parentComment.isCollapsed()) {
            collapsedCommentLevels.add(parentCommentDepth);
        }

        // Check to make sure the next comment isn't out of range of the full list
        if (position + 1 < totalItemCount) {
            // Retrieve first child comment
            int currentChildCommentPosition = position + 1;
            AbsComment currentChildComment = (AbsComment) mData.get(currentChildCommentPosition);
            int currentChildCommentDepth = currentChildComment.getDepth();
            // Loop through remaining comments until we reach another comment at same depth
            // as the parent, or we reach the end of the comment list
            while (currentChildCommentDepth > parentCommentDepth
                    && currentChildCommentPosition < totalItemCount) {
                // Loop through list of collapsed depths
                // If the current comment is less than a depth, remove that depth from the list
                for (int i = 0; i < collapsedCommentLevels.size(); i++) {
                    if (currentChildCommentDepth <= collapsedCommentLevels.get(i)) {
                        collapsedCommentLevels.remove(i);
                    }
                }
                // If the comment is collapsed, add it to the list of collapsed depths
                if (currentChildComment.isCollapsed()) {
                    collapsedCommentLevels.add(currentChildCommentDepth);
                }
                // Loop through collapsed depth list from parent depth until child depth
                // If any depth is collapsed less than child depth, set child to invisible
                boolean collapsedFromParent = false;
                for (int i = parentCommentDepth; i < currentChildCommentDepth; i++) {
                    if (collapsedCommentLevels.contains(i)) {
                        currentChildComment.setVisible(false);
                        collapsedFromParent = true;
                    }
                }
                // If comment was not collapsed from any collapsed parent, set visibility
                if (!collapsedFromParent) {
                    currentChildComment.setVisible(visible);
                }
                // Increment position
                currentChildCommentPosition++;
                // Retrieve comment at current position
                if (currentChildCommentPosition < totalItemCount) {
                    currentChildComment = (AbsComment) mData.get(currentChildCommentPosition);
                    currentChildCommentDepth = currentChildComment.getDepth();
                }
            }
        }

        syncVisibleData();
    }

    @Override
    public void collapseAllThreadsUnder(Integer score) {
        if (score == null)
            return;
        Listing current;
        Comment comment;
        for (int i = 0; i < mData.size(); i++) {
            current = mData.get(i);
            if (!(current instanceof Comment)) {
                continue;
            }
            comment = (Comment) current;
            if (comment.isCollapsed()) {
                continue;
            }
            if (comment.getScore() < score) {
                setThreadVisible(i, false);
            }
        }

        syncVisibleData();
    }
}