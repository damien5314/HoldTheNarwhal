/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.reddit.identity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserSettings {

    @Expose @SerializedName("beta") Boolean mBeta;
    @Expose @SerializedName("clickgadget") Boolean mClickgadget;
    @Expose @SerializedName("collapse_read_messages") Boolean mCollapseReadMessages;
    @Expose @SerializedName("compress") Boolean mCompress;
    @Expose @SerializedName("creddit_autorenew") Boolean mCredditAutoRenew;
    @Expose @SerializedName("default_comment_sort") String mDefaultCommentSort;
    @Expose @SerializedName("domain_details") Boolean mDomainDetails;
    @Expose @SerializedName("email_messages") Boolean mEmailMessages;
    @Expose @SerializedName("enable_default_themes") Boolean mEnableDefaultThemes;
    @Expose @SerializedName("hide_ads") Boolean mHideAds;
    @Expose @SerializedName("hide_downs") Boolean mHideDowns;
    @Expose @SerializedName("hide_from_robots") Boolean mHideFromRobots;
    @Expose @SerializedName("hide_locationbar") Boolean mHideLocationBar;
    @Expose @SerializedName("hide_ups") Boolean mHideUps;
    @Expose @SerializedName("highlight_controversial") Boolean mHighlightControversial;
    @Expose @SerializedName("highlight_new_comments") Boolean mHighlightNewComments;
    @Expose @SerializedName("ignore_suggested_sort") Boolean mIgnoreSuggestedSort;
    @Expose @SerializedName("label_nsfw") Boolean mLabelNsfw;
    @Expose @SerializedName("lang") String mLang;
    @Expose @SerializedName("mark_messages_read") Boolean mMarkMessagesRead;
    @Expose @SerializedName("media") String mMedia;
    @Expose @SerializedName("min_comment_score") Integer mMinCommentScore;
    @Expose @SerializedName("min_link_score") Integer mMinLinkScore;
    @Expose @SerializedName("monitor_mentions") Boolean mMonitorMentions;
    @Expose @SerializedName("newwindow") Boolean mNewWindow;
    @Expose @SerializedName("no_profanity") Boolean mNoProfanity;
    @Expose @SerializedName("num_comments") Integer mNumComments;
    @Expose @SerializedName("numsites") Integer mNumLinks;
    @Expose @SerializedName("organic") Boolean mOrganic;
    @Expose @SerializedName("over_18") Boolean mOver18;
    @Expose @SerializedName("public_feeds") Boolean mPublicFeeds;
    @Expose @SerializedName("public_votes") Boolean mPublicVotes;
    @Expose @SerializedName("research") Boolean mResearch;
    @Expose @SerializedName("show_flair") Boolean mShowFlair;
    @Expose @SerializedName("show_gold_expiration") Boolean mShowGoldExpiration;
    @Expose @SerializedName("show_link_flair") Boolean mShowLinkFlair;
    @Expose @SerializedName("show_promote") Boolean mShowPromote;
    @Expose @SerializedName("show_stylesheets") Boolean mShowStylesheets;
    @Expose @SerializedName("show_trending") Boolean mShowTrending;
    @Expose @SerializedName("store_visits") Boolean mStoreVisits;
    @Expose @SerializedName("theme_selector") String mThemeSelector;
    @Expose @SerializedName("threaded_messages") Boolean mThreadedMessages;
    @Expose @SerializedName("use_global_defaults") Boolean mUseGlobalDefaults;

    public Boolean getBeta() {
        return mBeta;
    }

    public Boolean getClickgadget() {
        return mClickgadget;
    }

    public Boolean getCollapseReadMessages() {
        return mCollapseReadMessages;
    }

    public Boolean getCompress() {
        return mCompress;
    }

    public Boolean getCredditAutoRenew() {
        return mCredditAutoRenew;
    }

    public String getDefaultCommentSort() {
        return mDefaultCommentSort;
    }

    public Boolean getDomainDetails() {
        return mDomainDetails;
    }

    public Boolean getEmailMessages() {
        return mEmailMessages;
    }

    public Boolean getEnableDefaultThemes() {
        return mEnableDefaultThemes;
    }

    public Boolean getHideAds() {
        return mHideAds;
    }

    public Boolean getHideDowns() {
        return mHideDowns;
    }

    public Boolean getHideFromRobots() {
        return mHideFromRobots;
    }

    public Boolean getHideLocationBar() {
        return mHideLocationBar;
    }

    public Boolean getHideUps() {
        return mHideUps;
    }

    public Boolean getHighlightControversial() {
        return mHighlightControversial;
    }

    public Boolean getHighlightNewComments() {
        return mHighlightNewComments;
    }

    public Boolean getIgnoreSuggestedSort() {
        return mIgnoreSuggestedSort;
    }

    public Boolean getLabelNsfw() {
        return mLabelNsfw;
    }

    public String getLang() {
        return mLang;
    }

    public Boolean getMarkMessagesRead() {
        return mMarkMessagesRead;
    }

    public String getMedia() {
        return mMedia;
    }

    public Integer getMinCommentScore() {
        return mMinCommentScore;
    }

    public Integer getMinLinkScore() {
        return mMinLinkScore;
    }

    public Boolean getMonitorMentions() {
        return mMonitorMentions;
    }

    public Boolean getNewWindow() {
        return mNewWindow;
    }

    public Boolean getNoProfanity() {
        return mNoProfanity;
    }

    public Integer getNumComments() {
        return mNumComments;
    }

    public Integer getNumLinks() {
        return mNumLinks;
    }

    public Boolean getOrganic() {
        return mOrganic;
    }

    public Boolean getOver18() {
        return mOver18;
    }

    public Boolean getPublicFeeds() {
        return mPublicFeeds;
    }

    public Boolean getPublicVotes() {
        return mPublicVotes;
    }

    public Boolean getResearch() {
        return mResearch;
    }

    public Boolean getShowFlair() {
        return mShowFlair;
    }

    public Boolean getShowGoldExpiration() {
        return mShowGoldExpiration;
    }

    public Boolean getShowLinkFlair() {
        return mShowLinkFlair;
    }

    public Boolean getShowPromote() {
        return mShowPromote;
    }

    public Boolean getShowStylesheets() {
        return mShowStylesheets;
    }

    public Boolean getShowTrending() {
        return mShowTrending;
    }

    public Boolean getStoreVisits() {
        return mStoreVisits;
    }

    public String getThemeSelector() {
        return mThemeSelector;
    }

    public Boolean getThreadedMessages() {
        return mThreadedMessages;
    }

    public Boolean getUseGlobalDefaults() {
        return mUseGlobalDefaults;
    }
}
