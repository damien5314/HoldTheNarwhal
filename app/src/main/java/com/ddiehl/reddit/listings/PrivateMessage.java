package com.ddiehl.reddit.listings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel @SuppressWarnings("unused")
public class PrivateMessage extends Listing {
  @Expose
  Data data;

  @Override
  public String getId() {
    return data.id;
  }

  public String getBody() {
    return data.body;
  }

  public boolean wasComment() {
    return data.wasComment == null ? false : data.wasComment;
  }

  public String getFirstMessage() {
    return data.firstMessage;
  }

  public String getFullname() {
    return data.fullname;
  }

  public String getFirstMessageName() {
    return data.firstMessageName;
  }

  public Long getCreated() {
    return data.created;
  }

  public String getDestination() {
    return data.dest;
  }

  public String getAuthor() {
    return data.author;
  }

  public Long getCreatedUtc() {
    return data.createdUtc;
  }

  public String getBodyHtml() {
    return data.bodyHtml;
  }

  public String getSubreddit() {
    return data.subreddit;
  }

  public String getParentId() {
    return data.parentId;
  }

  public String getContext() {
    return data.context;
  }

  public ListingResponse getReplies() {
    return data.replies;
  }

  public Boolean isUnread() {
    return data.isUnread;
  }

  public void markUnread(boolean b) {
    data.isUnread = b;
  }

  public String getDistinguished() {
    return data.distinguished;
  }

  public String getSubject() {
    return data.subject;
  }

  @Parcel
  public static class Data extends ListingData {
    @Expose
    String body;
    @SerializedName("was_comment")
    Boolean wasComment;
    @SerializedName("first_message")
    String firstMessage;
    @Expose @SerializedName("name")
    String fullname;
    @SerializedName("first_message_name")
    String firstMessageName;
    @Expose 
    Long created;
    @Expose
    String dest;
    @Expose
    String author;
    @Expose @SerializedName("created_utc")
    Long createdUtc;
    @SerializedName("body_html")
    String bodyHtml;
    @Expose
    String subreddit;
    @Expose @SerializedName("parent_id")
    String parentId;
    String context;
    @Expose
    ListingResponse replies;
    @Expose @SerializedName("new")
    Boolean isUnread;
    @Expose
    String distinguished;
    @Expose
    String subject;
  }
}