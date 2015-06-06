package com.ddiehl.reddit.listings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;

public abstract class AbsRedditComment<T extends AbsRedditComment.Data> extends Listing<T> {

    private int depth;
    private boolean isVisible = true;
    private boolean isCollapsed = false;

    public int getDepth() {
        return this.depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public boolean isVisible() {
        return this.isVisible;
    }

    public void setVisible(boolean b) {
        this.isVisible = b;
    }

    public boolean isCollapsed() {
        return this.isCollapsed;
    }

    public void setCollapsed(boolean b) {
        this.isCollapsed = b;
    }

    public String getParentId() {
        return data.parentId;
    }

    public static abstract class Data extends Listing.Data {
        @Expose @SerializedName("parent_id") String parentId;
    }

    public abstract String getSubredditId();
    public abstract Object getBannedBy();
    public abstract String getLinkId();
    public abstract Boolean isLiked();
    public abstract void isLiked(Boolean b);
    public abstract ListingResponse getReplies();
    public abstract void setReplies(ListingResponse response);
    public abstract List<Object> getUserReports();
    public abstract Boolean isSaved();
    public abstract void isSaved(boolean b);
    public abstract Integer getGilded();
    public abstract Boolean isArchived();
    public abstract Object getReportReasons();
    public abstract String getAuthor();
    public abstract int getScore();
    public abstract Object getApprovedBy();
    public abstract int getControversiality();
    public abstract String getBody();
    public abstract String isEdited();
    public abstract void isEdited(String s);
    public abstract String getAuthorFlairCssClass();
    public abstract int getDowns();
    public abstract String getBodyHtml();
    public abstract String getSubreddit();
    public abstract boolean isScoreHidden();
    public abstract double getCreated();
    public abstract String getAuthorFlairText();
    public abstract Double getCreateUtc();
    public abstract int getUps();
    public abstract List<Object> getModReports();
    public abstract Object getNumReports();
    public abstract String getDistinguished();
    public abstract Integer getCount();
    public abstract List<String> getChildren();

    public static class Utils {
        /**
         * Flattens list of comments, marking each comment with depth
         */
        public static void flattenCommentList(List<? extends AbsRedditComment> commentList) {
            int i = 0;
            while (i < commentList.size()) {
                AbsRedditComment listing = commentList.get(i);
                if (listing instanceof RedditComment) {
                    RedditComment comment = (RedditComment) listing;
                    ListingResponse repliesListing = comment.getReplies();
                    if (repliesListing != null) {
                        List<AbsRedditComment> replies = repliesListing.getData().getChildren();
                        flattenCommentList(replies);
                    }
                    comment.setDepth(comment.getDepth() + 1); // Increase depth by 1
                    if (comment.getReplies() != null) {
                        commentList.addAll(i+1, comment.getReplies().getData().getChildren()); // Add all of the replies to commentList
                        comment.setReplies(null); // Remove replies for comment
                    }
                } else { // Listing is a RedditMoreComments
                    RedditMoreComments moreComments = (RedditMoreComments) listing;
                    moreComments.setDepth(moreComments.getDepth() + 1); // Increase depth by 1
                }
                i++;
            }
        }

        /**
         * Sets depth for comments in a flat comments list
         */
        public static void setDepthForCommentsList(List<AbsRedditComment> comments, int parentDepth) {
            HashMap<String, Integer> depthMap = new HashMap<>();

            for (AbsRedditComment comment : comments) {
                String name = comment.getName();
                String parentId = comment.getParentId();
                if (depthMap.containsKey(parentId)) {
                    comment.setDepth(depthMap.get(parentId) + 1);
                } else {
                    comment.setDepth(parentDepth);
                }
                depthMap.put(name, comment.getDepth());
            }
        }
    }
}
