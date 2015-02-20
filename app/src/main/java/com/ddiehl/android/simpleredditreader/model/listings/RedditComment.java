package com.ddiehl.android.simpleredditreader.model.listings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


@SuppressWarnings("unused")
public class RedditComment extends Comment {

    @Expose
    private RedditCommentData data;


    @Override
    public Object getData() {
        return data;
    }

    public String getSubredditId() {
        return data.subredditId;
    }

    public Object getBannedBy() {
        return data.bannedBy;
    }

    public String getLinkId() {
        return data.linkId;
    }

    public Object getLikes() {
        return data.likes;
    }

    public ListingResponse<Listing> getReplies() {
        return data.replies;
    }

    public List<Object> getUserReports() {
        return data.userReports;
    }

    public boolean isSaved() {
        return data.saved;
    }

    public String getId() {
        return data.id;
    }

    public int getGilded() {
        return data.gilded;
    }

    public boolean isArchived() {
        return data.archived;
    }

    public Object getReportReasons() {
        return data.reportReasons;
    }

    public String getAuthor() {
        return data.author;
    }

    public String getParentId() {
        return data.parentId;
    }

    public int getScore() {
        return data.score;
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

    public String getAuthorFlairCssClass() {
        return data.AuthorFlairCssClass;
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
        return data.scoreHidden;
    }

    public String getName() {
        return data.name;
    }

    public double getCreated() {
        return data.created;
    }

    public String getAuthorFlairText() {
        return data.authorFlairText;
    }

    public double getCreateUtc() {
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

    public Object getDistinguished() {
        return data.distinguished;
    }

    private static class RedditCommentData {

        @Expose
        private ListingResponse<Listing> replies;
        @Expose @SerializedName("subreddit_id")
        private String subredditId;
        @Expose @SerializedName("banned_by")
        private Object bannedBy;
        @Expose @SerializedName("link_id")
        private String linkId;
        @Expose
        private Object likes;
        @Expose @SerializedName("user_reports")
        private List<Object> userReports;
        @Expose
        private boolean saved;
        @Expose
        private String id;
        @Expose
        private int gilded;
        @Expose
        private boolean archived;
        @Expose @SerializedName("report_reasons")
        private Object reportReasons;
        @Expose
        private String author;
        @Expose @SerializedName("parent_id")
        private String parentId;
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
        private String name;
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
        private Object distinguished;

    }
}
