package com.ddiehl.android.simpleredditreader.model.listings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommentBank {
    private static final String TAG = CommentBank.class.getSimpleName();

    private List<AbsRedditComment> mData;
    private List<AbsRedditComment> mVisibleData;

    public CommentBank() {
        mData = new ArrayList<>();
        mVisibleData = new ArrayList<>();
    }

    public CommentBank(List<AbsRedditComment> data) {
        mData = new ArrayList<>(data);
        mVisibleData = new ArrayList<>();
        syncVisibleData();
    }

    public boolean addAll(Collection<? extends AbsRedditComment> collection) {
        boolean result = mData.addAll(collection);
        syncVisibleData();
        return result;
    }

    public boolean addAll(int index, Collection<? extends AbsRedditComment> collection) {
        boolean result = mData.addAll(index, collection);
        syncVisibleData();
        return result;
    }

    public int indexOf(AbsRedditComment comment) {
        return mData.indexOf(comment);
    }

    public AbsRedditComment get(int position) {
        return mData.get(position);
    }

    public int size() {
        return mData.size();
    }

    public AbsRedditComment remove(int position) {
        AbsRedditComment result = mData.remove(position);
        syncVisibleData();
        return result;
    }

    public boolean remove(AbsRedditComment comment) {
        boolean removed = mData.remove(comment);
        syncVisibleData();
        return removed;
    }

    public void clear() {
        mData.clear();
        syncVisibleData();
    }

    public void setData(List<AbsRedditComment> data) {
        clear();
        addAll(data);
        syncVisibleData();
    }

    public boolean isVisible(int position) {
        return mData.get(position).isVisible();
    }

    public int getNumVisible() {
        return mVisibleData.size();
    }

    public AbsRedditComment getVisibleComment(int position) {
        return mVisibleData.get(position);
    }

    private void syncVisibleData() {
        mVisibleData.clear();
        for (AbsRedditComment comment : mData) {
            if (comment.isVisible()) {
                mVisibleData.add(comment);
            }
        }
    }

    public void setThreadVisible(int position, boolean visible) {
        int totalItemCount = mData.size();
        RedditComment parentComment = (RedditComment) mData.get(position);
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
            AbsRedditComment currentChildComment = (AbsRedditComment) mData.get(currentChildCommentPosition);
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
                    currentChildComment = (AbsRedditComment) mData.get(currentChildCommentPosition);
                    currentChildCommentDepth = currentChildComment.getDepth();
                }
            }
        }

        syncVisibleData();
    }
}
