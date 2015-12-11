package com.ddiehl.reddit.listings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;

public abstract class AbsComment<T extends AbsComment.Data> extends Listing<T> {

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
    public static void flattenCommentList(List<Listing> commentList) {
      int i = 0;
      while (i < commentList.size()) {
        Listing listing = commentList.get(i);
        if (listing instanceof Comment) {
          Comment comment = (Comment) listing;
          ListingResponse repliesListing = comment.getReplies();
          if (repliesListing != null) {
            List<Listing> replies = repliesListing.getData().getChildren();
            flattenCommentList(replies);
          }
          comment.setDepth(comment.getDepth() + 1); // Increase depth by 1
          if (comment.getReplies() != null) {
            commentList.addAll(i + 1, comment.getReplies().getData().getChildren()); // Add all of the replies to commentList
            comment.setReplies(null); // Remove replies for comment
          }
        } else { // Listing is a CommentStub
          CommentStub moreComments = (CommentStub) listing;
          moreComments.setDepth(moreComments.getDepth() + 1); // Increase depth by 1
        }
        i++;
      }
    }

    /**
     * Sets depth for comments in a flat comments list
     */
    public static void setDepthForCommentsList(List<Listing> comments, int parentDepth) {
      HashMap<String, Integer> depthMap = new HashMap<>();

      for (Listing listing : comments) {
        AbsComment comment = (AbsComment) listing;
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
