package com.ddiehl.reddit.listings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;

public abstract class AbsRedditComment<T extends AbsRedditComment.Data> extends Listing<T> {

    private int depth;
    private boolean isVisible = true;

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

    public abstract boolean isCollapsed();
    public abstract void setCollapsed(boolean b);

    public String getParentId() {
        return data.parentId;
    }

    public static abstract class Data extends Listing.Data {
        @Expose @SerializedName("parent_id") String parentId;
    }

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
