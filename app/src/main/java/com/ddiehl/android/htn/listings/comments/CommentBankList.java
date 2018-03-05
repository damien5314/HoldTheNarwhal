package com.ddiehl.android.htn.listings.comments;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rxreddit.model.AbsComment;
import rxreddit.model.Comment;
import rxreddit.model.Listing;

public class CommentBankList implements CommentBank {

    private final List<Listing> data;
    private final List<Listing> visibleData;

    public CommentBankList() {
        data = new ArrayList<>();
        visibleData = new ArrayList<>();
    }

    @Override
    public void add(AbsComment comment) {
        data.add(comment);
        syncVisibleData();
    }

    @Override
    public void add(int index, AbsComment comment) {
        data.add(index, comment);
        syncVisibleData();
    }

    @Override
    public boolean addAll(Collection<Listing> collection) {
        boolean result = data.addAll(collection);
        syncVisibleData();
        return result;
    }

    @Override
    public boolean addAll(int index, Collection<Listing> collection) {
        boolean result = data.addAll(index, collection);
        syncVisibleData();
        return result;
    }

    @Override
    public int indexOf(AbsComment o) {
        return data.indexOf(o);
    }

    @Override
    public int visibleIndexOf(AbsComment obj) {
        return visibleData.indexOf(obj);
    }

    @Override
    public AbsComment get(int position) {
        return (AbsComment) data.get(position);
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public AbsComment remove(int position) {
        AbsComment result = (AbsComment) data.remove(position);
        syncVisibleData();
        return result;
    }

    @Override
    public boolean remove(AbsComment comment) {
        boolean removed = data.remove(comment);
        syncVisibleData();
        return removed;
    }

    @Override
    public void clear() {
        data.clear();
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
        return ((AbsComment) data.get(position)).isVisible();
    }

    @Override
    public int getNumVisible() {
        return visibleData.size();
    }

    @Override
    public AbsComment getVisibleComment(int position) {
        return (AbsComment) visibleData.get(position);
    }

    private void syncVisibleData() {
        visibleData.clear();
        for (Listing comment : data) {
            if (((AbsComment) comment).isVisible()) {
                visibleData.add(comment);
            }
        }
    }

    @Override
    public void toggleThreadVisible(Comment comment) {
        int position = indexOf(comment);
        boolean visible = comment.isCollapsed();
        setThreadVisible(position, visible);
    }

    private void setThreadVisible(int position, boolean visible) {
        int totalItemCount = data.size();
        Comment parentComment = (Comment) data.get(position);
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
            AbsComment currentChildComment = (AbsComment) data.get(currentChildCommentPosition);
            int currentChildCommentDepth = currentChildComment.getDepth();
            // Loop through remaining comments until we reach another comment at same or lesser
            // depth as the parent, or we reach the end of the comment list
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
                    currentChildComment = (AbsComment) data.get(currentChildCommentPosition);
                    currentChildCommentDepth = currentChildComment.getDepth();
                }
            }
        }

        syncVisibleData();
    }

    @Override
    public void collapseAllThreadsUnder(@Nullable Integer score) {
        if (score == null) return;

        for (int i = 0; i < data.size(); i++) {
            Listing current = data.get(i);

            // Check if we're actually looking at a comment and not a stub
            if (!(current instanceof Comment)) {
                continue;
            }
            Comment comment = (Comment) current;

            // If the comment is collaped, it's already hidden, ignore
            if (comment.isCollapsed()) {
                continue;
            }

            // If comment has score and it's less than the minimum score, hide it
            if (comment.getScore() != null && comment.getScore() < score) {
                setThreadVisible(i, false);
            }
        }

        syncVisibleData();
    }
}
