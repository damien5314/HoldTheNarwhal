package com.ddiehl.reddit.listings;

import com.ddiehl.reddit.Savable;
import com.ddiehl.reddit.Votable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@SuppressWarnings("unused")
public class Comment extends AbsComment implements Votable, Savable {
  @Expose
  Data data;
  private boolean isCollapsed = false;

  @Override
  public String getId() {
    return data.id;
  }

  public String getUrl() {
    return String.format("http://www.reddit.com/r/%s/comments/%s?comment=%s",
        getSubreddit(),
        getLinkId().substring(3), // Remove the type prefix (t3_, etc)
        getId());
  }

  @Override
  public boolean isCollapsed() {
    return this.isCollapsed;
  }

  @Override
  public void setCollapsed(boolean b) {
    this.isCollapsed = b;
  }

  public String getSubredditId() {
    return data.subredditId;
  }

  public Object getBannedBy() {
    return data.bannedBy;
  }

  public String getLinkId() {
    // This is a hack to get the linkId from the context
    // because comments in the inbox do not have the linkId for some reason
    String linkId = data.linkId;
    if (linkId == null) {
      String linkUrl = data.context;
      int i = linkUrl.indexOf("/comments/") + 10; // Add length of string
      int j = linkUrl.indexOf("/", i + 1);
      data.linkId = linkUrl.substring(i, j);
    }
    int i = data.linkId.indexOf('_');
    if (i != -1) return data.linkId.substring(i + 1);
    return data.linkId;
  }

  @Override
  public Boolean isLiked() {
    return data.isLiked;
  }

  @Override
  public void isLiked(Boolean b) {
    data.isLiked = b;
  }

  public ListingResponse getReplies() {
    return data.replies;
  }

  public void setReplies(ListingResponse response) {
    data.replies = response;
  }

  public List<Object> getUserReports() {
    return data.userReports;
  }

  @Override
  public boolean isSaved() {
    return data.saved == null ? false : data.saved;
  }

  @Override
  public void isSaved(boolean b) {
    data.saved = b;
  }

  public Integer getGilded() {
    return data.gilded;
  }

  @Override
  public boolean isArchived() {
    return data.isArchived == null ? false : data.isArchived;
  }

  public Object getReportReasons() {
    return data.reportReasons;
  }

  public String getAuthor() {
    return data.author;
  }

  public void setAuthor(String author) {
    data.author = author;
  }

  public String getAuthorFlairCssClass() {
    return data.AuthorFlairCssClass;
  }

  public String getAuthorFlairText() {
    return data.authorFlairText;
  }

  public Integer getScore() {
    return data.score;
  }

  public void setScore(Integer score) {
    data.score = score;
  }

  public Object getApprovedBy() {
    return data.approvedBy;
  }

  public int getControversiality() {
    return data.controversiality;
  }

  public String getBody() {
    return data.body;
  }

  public String isEdited() {
    return data.edited;
  }

  public int getDowns() {
    return data.downs;
  }

  public String getBodyHtml() {
    return data.bodyHtml;
  }

  public String getSubreddit() {
    return data.subreddit;
  }

  public boolean isScoreHidden() {
    return data.hideScore == null ? false : data.hideScore;
  }

  public double getCreated() {
    return data.created;
  }

  public Double getCreateUtc() {
    return data.createUtc;
  }

  public int getUps() {
    return data.ups;
  }

  public List<Object> getModReports() {
    return data.modReports;
  }

  public Object getNumReports() {
    return data.numReports;
  }

  public String getDistinguished() {
    return data.distinguished;
  }

  @Override
  public void applyVote(int direction) {
    int scoreDiff = direction - getLikedScore();
    switch (direction) {
      case 0: isLiked(null); break;
      case 1: isLiked(true); break;
      case -1: isLiked(false); break;
    }
    if (data.score == null) return;
    data.score += scoreDiff;
  }

  private int getLikedScore() {
    if (isLiked() == null)
      return 0;
    else if (isLiked())
      return 1;
    else
      return -1;
  }

  public String getLinkTitle() {
    return data.linkTitle;
  }

  public String getRemovalReason() {
    return data.removalReason;
  }

  public String getLinkAuthor() {
    return data.linkAuthor;
  }

  @Override
  public String getParentId() {
    // FIXME Consider trimming the parentId from this before returning, for consistency
    // with other Listing types
    return data.parentId;
  }

  public String getLinkUrl() {
    return data.linkUrl;
  }

  public String getSubject() {
    return data.subject;
  }

  public String getContext() {
    return data.context;
  }

  public static class Data extends AbsComment.Data {
    // Attributes specific to listing views
    @Expose @SerializedName("link_title")
    private String linkTitle;
    @SerializedName("removal_reason")
    private String removalReason;
    @Expose @SerializedName("link_author")
    private String linkAuthor;
    @Expose @SerializedName("link_url")
    private String linkUrl;

    // Attributes common to all comment views
    @Expose
    private ListingResponse replies;
    @SerializedName("subreddit_id")
    private String subredditId;
    @SerializedName("banned_by")
    private Object bannedBy;
    @Expose @SerializedName("link_id")
    private String linkId;
    @Expose @SerializedName("likes")
    private Boolean isLiked;
    @SerializedName("user_reports")
    private List<Object> userReports;
    @Expose
    private Boolean saved;
    @Expose
    private Integer gilded;
    @Expose @SerializedName("archived")
    private Boolean isArchived;
    @SerializedName("report_reasons")
    private Object reportReasons;
    @Expose
    private String author;
    @Expose
    private Integer score;
    @SerializedName("approved_by")
    private Object approvedBy;
    private int controversiality;
    @Expose
    private String body;
    @Expose
    private String edited;
    @SerializedName("author_flair_css_class")
    private String AuthorFlairCssClass;
    private int downs;
    @SerializedName("body_html")
    private String bodyHtml;
    @Expose
    private String subreddit;
    @Expose @SerializedName("score_hidden")
    private Boolean hideScore;
    private double created;
    @SerializedName("author_flair_text")
    private String authorFlairText;
    @Expose @SerializedName("created_utc")
    private double createUtc;
    private int ups;
    @SerializedName("mod_reports")
    private List<Object> modReports;
    @SerializedName("num_reports")
    private Object numReports;
    @Expose
    private String distinguished;
    @Expose
    private String subject;
    @Expose
    private String context;
  }

  @Override
  public String toString() {
    return "Comment: " + getAuthor() + " - depth " + getDepth();
  }
}