package com.ddiehl.reddit.listings;

import com.google.gson.annotations.Expose;

import java.util.List;

@SuppressWarnings("unused")
public class RedditMoreComments extends AbsRedditComment<RedditMoreComments.Data> {

    @Override
    public Integer getCount() {
        return data.count;
    }

    @Override
    public List<String> getChildren() {
        return data.children;
    }

    @Override
    public String getSubredditId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getBannedBy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLinkId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean isLiked() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void isLiked(Boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListingResponse getReplies() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setReplies(ListingResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Object> getUserReports() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean isSaved() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void isSaved(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer getGilded() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean isArchived() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getReportReasons() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAuthor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getScore() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getApprovedBy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getControversiality() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getBody() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String isEdited() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void isEdited(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAuthorFlairCssClass() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getDowns() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getBodyHtml() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSubreddit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isScoreHidden() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getCreated() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAuthorFlairText() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Double getCreateUtc() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getUps() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Object> getModReports() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getNumReports() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDistinguished() {
        throw new UnsupportedOperationException();
    }

    public static class Data extends AbsRedditComment.Data {
        @Expose private Integer count;
        @Expose private List<String> children;
    }

    @Override
    public String toString() {
        return "MoreComments (" + getCount() + ")";
    }
}
