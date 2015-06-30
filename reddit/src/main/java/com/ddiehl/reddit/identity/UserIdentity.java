/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.reddit.identity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserIdentity {

    // Variables for response from me.json
    @Expose @SerializedName("has_mail") private Boolean hasMail;
    @Expose private String name;
    @Expose private Long created;
    @Expose @SerializedName("hide_from_robots") private Boolean hideFromRobots;
    @Expose @SerializedName("gold_creddits") private Integer goldCreddits;
    @Expose @SerializedName("created_utc") private Long createdUTC;
    @Expose @SerializedName("has_mod_mail") private Boolean hasModMail;
    @Expose @SerializedName("link_karma") private Integer linkKarma;
    @Expose @SerializedName("comment_karma") private Integer commentKarma;
    @Expose @SerializedName("over_18") private Boolean isOver18;
    @Expose @SerializedName("is_gold") private Boolean isGold;
    @Expose @SerializedName("is_mod") private Boolean isMod;
    @Expose @SerializedName("gold_expiration") private Long goldExpiration;
    @Expose @SerializedName("has_verified_email") private Boolean hasVerifiedEmail;
    @Expose private String id;
    @Expose @SerializedName("inbox_count") private Integer inboxCount;

    public Boolean hasMail() {
        return hasMail;
    }

    public void hasMail(Boolean hasMail) {
        this.hasMail = hasMail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public Boolean isHiddenFromRobots() {
        return hideFromRobots;
    }

    public void isHiddenFromRobots(Boolean hideFromRobots) {
        this.hideFromRobots = hideFromRobots;
    }

    public Integer getGoldCreddits() {
        return goldCreddits;
    }

    public void setGoldCreddits(Integer goldCreddits) {
        this.goldCreddits = goldCreddits;
    }

    public Long getCreatedUTC() {
        return createdUTC;
    }

    public void setCreatedUTC(Long createdUTC) {
        this.createdUTC = createdUTC;
    }

    public Boolean hasModMail() {
        return hasModMail;
    }

    public void hasModMail(Boolean hasModMail) {
        this.hasModMail = hasModMail;
    }

    public Integer getLinkKarma() {
        return linkKarma;
    }

    public void setLinkKarma(Integer linkKarma) {
        this.linkKarma = linkKarma;
    }

    public Integer getCommentKarma() {
        return commentKarma;
    }

    public void setCommentKarma(Integer commentKarma) {
        this.commentKarma = commentKarma;
    }

    public Boolean isOver18() {
        return isOver18;
    }

    public void isOver18(Boolean isOver18) {
        this.isOver18 = isOver18;
    }

    public Boolean isGold() {
        return isGold;
    }

    public void isGold(Boolean isGold) {
        this.isGold = isGold;
    }

    public Boolean isMod() {
        return isMod;
    }

    public void isMod(Boolean isMod) {
        this.isMod = isMod;
    }

    public Long getGoldExpiration() {
        return goldExpiration;
    }

    public void setGoldExpiration(Long goldExpiration) {
        this.goldExpiration = goldExpiration;
    }

    public Boolean hasVerifiedEmail() {
        return hasVerifiedEmail;
    }

    public void hasVerifiedEmail(Boolean hasVerifiedEmail) {
        this.hasVerifiedEmail = hasVerifiedEmail;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getInboxCount() {
        return inboxCount;
    }

    public void setInboxCount(Integer inboxCount) {
        this.inboxCount = inboxCount;
    }

    @Override
    public String toString() {
        return getId() + " - " + getName() + " - Gold: " + isGold();
    }
}
