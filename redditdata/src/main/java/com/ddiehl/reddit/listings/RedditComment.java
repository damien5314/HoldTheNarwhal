package com.ddiehl.reddit.listings;

import com.ddiehl.reddit.Savable;
import com.ddiehl.reddit.Votable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@SuppressWarnings("unused")
public class RedditComment extends AbsRedditComment<RedditComment.Data> implements Votable, Savable {

    public String getUrl() {
        return String.format("http://www.reddit.com/r/%s/comments/%s?comment=%s",
                getSubreddit(),
                getLinkId().substring(3), // Remove the type prefix (t3_, etc)
                getId());
    }

    @Override
    public String getSubredditId() {
        return data.subredditId;
    }

    @Override
    public Object getBannedBy() {
        return data.bannedBy;
    }

    @Override
    public String getLinkId() {
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

    @Override
    public ListingResponse getReplies() {
        return data.replies;
    }

    @Override
    public void setReplies(ListingResponse response) {
        data.replies = response;
    }

    @Override
    public List<Object> getUserReports() {
        return data.userReports;
    }

    @Override
    public Boolean isSaved() {
        return data.saved;
    }

    @Override
    public void isSaved(boolean b) {
        data.saved = b;
    }

    @Override
    public Integer getGilded() {
        return data.gilded;
    }

    @Override
    public Boolean isArchived() {
        return data.isArchived;
    }

    @Override
    public Object getReportReasons() {
        return data.reportReasons;
    }

    @Override
    public String getAuthor() {
        return data.author;
    }

    @Override
    public int getScore() {
        return data.score;
    }

    @Override
    public Object getApprovedBy() {
        return data.approvedBy;
    }

    @Override
    public int getControversiality() {
        return data.controversiality;
    }

    @Override
    public String getBody() {
        return data.body;
    }

    @Override
    public String isEdited() {
        return data.edited;
    }

    @Override
    public void isEdited(String s) {
        data.edited = s;
    }

    @Override
    public String getAuthorFlairCssClass() {
        return data.AuthorFlairCssClass;
    }

    @Override
    public int getDowns() {
        return data.downs;
    }

    @Override
    public String getBodyHtml() {
        return data.bodyHtml;
    }

    @Override
    public String getSubreddit() {
        return data.subreddit;
    }

    @Override
    public boolean isScoreHidden() {
        return data.scoreHidden;
    }

    @Override
    public double getCreated() {
        return data.created;
    }

    @Override
    public String getAuthorFlairText() {
        return data.authorFlairText;
    }

    @Override
    public Double getCreateUtc() {
        return data.createUtc;
    }

    @Override
    public int getUps() {
        return data.ups;
    }

    @Override
    public List<Object> getModReports() {
        return data.modReports;
    }

    @Override
    public Object getNumReports() {
        return data.numReports;
    }

    @Override
    public String getDistinguished() {
        return data.distinguished;
    }

    @Override
    public Integer getCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getChildren() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void applyVote(int direction) {
        int scoreDiff = direction - getLikedScore();
        data.score += scoreDiff;
        switch (direction) {
            case 0: isLiked(null); break;
            case 1: isLiked(true); break;
            case -1: isLiked(false); break;
        }
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

    public String getParentId() {
        return data.parentId;
    }

    public String getLinkUrl() {
        return data.linkUrl;
    }

    public static class Data extends AbsRedditComment.Data {

        // Attributes specific to listing views
        @Expose @SerializedName("link_title")
        private String linkTitle;
        @Expose @SerializedName("removal_reason")
        private String removalReason;
        @Expose @SerializedName("link_author")
        private String linkAuthor;
        @Expose @SerializedName("link_url")
        private String linkUrl;

        @Expose
        private ListingResponse replies;
        @Expose @SerializedName("subreddit_id")
        private String subredditId;
        @Expose @SerializedName("banned_by")
        private Object bannedBy;
        @Expose @SerializedName("link_id")
        private String linkId;
        @Expose @SerializedName("likes")
        private Boolean isLiked;
        @Expose @SerializedName("user_reports")
        private List<Object> userReports;
        @Expose
        private Boolean saved;
        @Expose
        private Integer gilded;
        @Expose @SerializedName("archived")
        private Boolean isArchived;
        @Expose @SerializedName("report_reasons")
        private Object reportReasons;
        @Expose
        private String author;
        @Expose
        private int score;
        @Expose @SerializedName("approved_by")
        private Object approvedBy;
        @Expose
        private int controversiality;
        @Expose
        private String body;
        @Expose
        private String edited;
        @Expose @SerializedName("author_flair_css_class")
        private String AuthorFlairCssClass;
        @Expose
        private int downs;
        @Expose @SerializedName("body_html")
        private String bodyHtml;
        @Expose
        private String subreddit;
        @Expose @SerializedName("score_hidden")
        private boolean scoreHidden;
        @Expose
        private double created;
        @Expose @SerializedName("author_flair_text")
        private String authorFlairText;
        @Expose @SerializedName("created_utc")
        private double createUtc;
        @Expose
        private int ups;
        @Expose @SerializedName("mod_reports")
        private List<Object> modReports;
        @Expose @SerializedName("num_reports")
        private Object numReports;
        @Expose
        private String distinguished;

    }

    @Override
    public String toString() {
        return "Comment: " + getAuthor() + " - " + "depth " + getDepth();
    }
}
