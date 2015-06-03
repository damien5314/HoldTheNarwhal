package com.ddiehl.reddit.listings;

import com.ddiehl.reddit.Hideable;
import com.ddiehl.reddit.Savable;
import com.ddiehl.reddit.Votable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("unused")
public class RedditLink extends Listing implements Votable, Savable, Hideable {

    @Expose
    private RedditLinkData data;

    @Override
    public RedditLinkData getData() {
        return data;
    }

    public String getDomain() {
        return data.domain;
    }

    public Object getBannedBy() {
        return data.bannedBy;
    }

    public RedditLinkData.MediaEmbed getMediaEmbed() {
        return data.mediaEmbed;
    }

    public String getSubreddit() {
        return data.subreddit;
    }

    public Object getSelftextHtml() {
        return data.selftextHtml;
    }

    public String getSelftext() {
        return data.selftext;
    }

    public String isEdited() {
        return data.edited;
    }

    public void isEdited(String s) {
        data.edited = s;
    }

    public Boolean isLiked() {
        return data.likes;
    }

    public void isLiked(Boolean b) {
        data.likes = b;
    }

    public List<Object> getUserReports() {
        return data.userReports;
    }

    public Object getSecureMedia() {
        return data.secureMedia;
    }

    public Object getLinkFlairText() {
        return data.linkFlairText;
    }

    @Override
    public String getId() {
        return data.id;
    }

    public Integer getGilded() {
        return data.gilded;
    }

    @Override
    public Boolean isArchived() {
        return data.isArchived;
    }

    public Boolean getClicked() {
        return data.clicked;
    }

    public Object getReportReasons() {
        return data.reportReasons;
    }

    public String getAuthor() {
        return data.author;
    }

    public Integer getNumComments() {
        return data.numComments;
    }

    public Integer getScore() {
        return data.score;
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

    public Object getApprovedBy() {
        return data.approvedBy;
    }

    public Boolean getOver18() {
        return data.over18;
    }

    @Override
    public Boolean isHidden() {
        return data.hidden;
    }

    @Override
    public void isHidden(Boolean b) {
        data.hidden = b;
    }

    public String getThumbnail() {
        return data.thumbnail;
    }

    public String getSubredditId() {
        return data.subredditId;
    }

    public Object getLinkFlairCssClass() {
        return data.linkFlairCssClass;
    }

    public Object getAuthorFlairCssClass() {
        return data.authorFlairCssClass;
    }

    public Integer getDowns() {
        return data.downs;
    }

    public RedditLinkData.SecureMediaEmbed getSecureMediaEmbed() {
        return data.secureMediaEmbed;
    }

    @Override
    public Boolean isSaved() {
        return data.saved;
    }

    @Override
    public void isSaved(boolean b) {
        data.saved = b;
    }

    public Boolean getStickied() {
        return data.stickied;
    }

    public Boolean isSelf() {
        return data.isSelf;
    }

    public String getPermalink() {
        return data.permalink;
    }

    @Override
    public String getName() {
        return data.name;
    }

    public Double getCreated() {
        return data.created;
    }

    public String getUrl() {
        return data.url;
    }

    public Object getAuthorFlairText() {
        return data.authorFlairText;
    }

    public String getTitle() {
        return data.title;
    }

    public Double getCreatedUtc() {
        return data.createdUtc;
    }

    public String getDistinguished() {
        return data.distinguished;
    }

    public RedditLinkData.Media getMedia() {
        return data.media;
    }

    public List<Object> getModReports() {
        return data.modReports;
    }

    public Boolean getVisited() {
        return data.visited;
    }

    public Object getNumReports() {
        return data.numReports;
    }

    public Integer getUps() {
        return data.ups;
    }

    public static class RedditLinkData {

        @Expose
        private String domain;
        @Expose @SerializedName("banned_by")
        private Object bannedBy;
        @Expose @SerializedName("media_embed")
        private MediaEmbed mediaEmbed;
        @Expose
        private String subreddit;
        @Expose @SerializedName("selftext_html")
        private String selftextHtml;
        @Expose
        private String selftext;
        @Expose
        private String edited;
        @Expose
        private Boolean likes;
        @Expose @SerializedName("user_reports")
        private List<Object> userReports = new ArrayList<>();
        @Expose @SerializedName("secure_media")
        private Object secureMedia;
        @Expose @SerializedName("link_flair_text")
        private Object linkFlairText;
        @Expose
        private String id;
        @Expose
        private Integer gilded;
        @Expose @SerializedName("archived")
        private Boolean isArchived;
        @Expose
        private Boolean clicked;
        @Expose @SerializedName("report_reasons")
        private Object reportReasons;
        @Expose
        private String author;
        @Expose @SerializedName("num_comments")
        private Integer numComments;
        @Expose
        private Integer score;
        @Expose @SerializedName("approved_by")
        private Object approvedBy;
        @Expose @SerializedName("over_18")
        private Boolean over18;
        @Expose
        private Boolean hidden;
        @Expose
        private String thumbnail;
        @Expose @SerializedName("subreddit_id")
        private String subredditId;
        @Expose @SerializedName("link_flair_css_class")
        private Object linkFlairCssClass;
        @Expose @SerializedName("author_flair_css_class")
        private Object authorFlairCssClass;
        @Expose
        private Integer downs;
        @Expose @SerializedName("secure_media_embed")
        private SecureMediaEmbed secureMediaEmbed;
        @Expose
        private Boolean saved;
        @Expose
        private Boolean stickied;
        @Expose @SerializedName("is_self")
        private Boolean isSelf;
        @Expose
        private String permalink;
        @Expose
        private String name;
        @Expose
        private Double created;
        @Expose
        private String url;
        @Expose @SerializedName("author_flair_text")
        private Object authorFlairText;
        @Expose
        private String title;
        @Expose @SerializedName("created_utc")
        private Double createdUtc;
        @Expose
        private String distinguished;
        @Expose
        private Media media;
        @Expose @SerializedName("mod_reports")
        private List<Object> modReports = new ArrayList<>();
        @Expose
        private Boolean visited;
        @Expose @SerializedName("num_reports")
        private Object numReports;
        @Expose
        private Integer ups;

        protected static class SecureMediaEmbed {

        }

        protected static class MediaEmbed {

            @Expose
            private String content;
            @Expose
            private Integer width;
            @Expose
            private Boolean scrolling;
            @Expose
            private Integer height;


            public String getContent() {
                return content;
            }

            public Integer getWidth() {
                return width;
            }

            public Boolean getScrolling() {
                return scrolling;
            }

            public Integer getHeight() {
                return height;
            }

        }

        protected static class Media {

            @Expose
            private Oembed oembed;
            @Expose
            private String type;


            public Oembed getOembed() {
                return oembed;
            }

            public String getType() {
                return type;
            }

            private static class Oembed {

                @Expose @SerializedName("provider_url")
                private String providerUrl;
                @Expose
                private String description;
                @Expose
                private String title;
                @Expose
                private String type;
                @Expose @SerializedName("thumbnail_width")
                private Integer thumbnailWidth;
                @Expose
                private Integer height;
                @Expose
                private Integer width;
                @Expose
                private String html;
                @Expose
                private String version;
                @Expose @SerializedName("provider_name")
                private String providerName;
                @Expose @SerializedName("thumbnail_url")
                private String thumbnailUrl;
                @Expose @SerializedName("thumbnail_height")
                private Integer thumbnailHeight;


                public String getProviderUrl() {
                    return providerUrl;
                }

                public String getDescription() {
                    return description;
                }

                public String getTitle() {
                    return title;
                }

                public String getType() {
                    return type;
                }

                public Integer getThumbnailWidth() {
                    return thumbnailWidth;
                }

                public Integer getHeight() {
                    return height;
                }

                public Integer getWidth() {
                    return width;
                }

                public String getHtml() {
                    return html;
                }

                public String getVersion() {
                    return version;
                }

                public String getProviderName() {
                    return providerName;
                }

                public String getThumbnailUrl() {
                    return thumbnailUrl;
                }

                public Integer getThumbnailHeight() {
                    return thumbnailHeight;
                }
            }
        }

    }

}