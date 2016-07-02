package com.ddiehl.android.htn.view.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.MessagePresenter;
import com.ddiehl.android.htn.utils.Utils;
import com.ddiehl.timesincetextview.TimeSince;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rxreddit.model.Listing;
import rxreddit.model.PrivateMessage;

public class ListingsMessageViewHolder extends RecyclerView.ViewHolder
    implements View.OnCreateContextMenuListener {

  @Inject protected Context mAppContext;
  private final MessagePresenter mInboxPresenter;
  private PrivateMessage mMessage;

  @BindView(R.id.conversation_subject) TextView mConversationSubject;
  @BindView(R.id.conversation_body_layout) ViewGroup mConversationBodyLayout;
  @BindView(R.id.collapsed_messages_layout) ViewGroup mCollapsedMessagesLayout;
  @BindView(R.id.collapsed_messages_text) TextView mCollapsedMessagesText;
  @BindView(R.id.last_message_layout) ViewGroup mLastMessageLayout;
  @BindView(R.id.message_indentation) View mMessageIndentation;
  @BindView(R.id.last_message_metadata) TextView mLastMessageMetadata;
  @BindView(R.id.unread_message_indicator) View mUnreadMessageIndicator;
  @BindView(R.id.last_message_body) TextView mLastMessageBody;

  public ListingsMessageViewHolder(View view, MessagePresenter presenter) {
    super(view);
    HoldTheNarwhal.getApplicationComponent().inject(this);
    mInboxPresenter = presenter;
    ButterKnife.bind(this, view);
    itemView.setOnCreateContextMenuListener(this);
  }

  public void bind(PrivateMessage message, boolean showReplies) {
    mMessage = message;
    List<Listing> replies = null;
    if (message.getReplies() != null) {
      replies = message.getReplies().getData().getChildren();
    }
    PrivateMessage messageToShow;
    if (!showReplies) {
      ((View) mConversationSubject.getParent()).setVisibility(View.GONE);
    }
    if (!showReplies || replies == null || replies.size() == 0) {
      messageToShow = mMessage;
      // Collapse the "view more messages" view
      mCollapsedMessagesLayout.setVisibility(View.GONE);
      // Hide the indentation and remove background
      mMessageIndentation.setVisibility(View.GONE);
    } else {
      messageToShow = (PrivateMessage) replies.get(replies.size()-1);
      // Expand the "view more messages" view
      mCollapsedMessagesLayout.setVisibility(View.VISIBLE);
      // Show the indentation and set background
      mMessageIndentation.setVisibility(View.VISIBLE);
    }
    // Show message metadata and text
    mConversationSubject.setText(messageToShow.getSubject());
    if (replies != null) {
      int n = replies.size();
      mCollapsedMessagesText.setText(
          mAppContext.getResources().getQuantityString(R.plurals.view_more_messages, n, n));
    }
    boolean isToMe = Utils.equals(
        mInboxPresenter.getUserIdentity().getName(), messageToShow.getDestination());
    String from = mAppContext.getString(
        isToMe ? R.string.message_metadata_from : R.string.message_metadata_to);
    from = String.format(from,
        isToMe ? messageToShow.getAuthor() : messageToShow.getDestination());
    String sent = mAppContext.getString(R.string.message_metadata_sent);
    sent = String.format(sent, TimeSince.getFormattedDateString(
        messageToShow.getCreatedUtc(), false, mAppContext));
    String text = from + " " + sent;
    mLastMessageMetadata.setText(text);
    mLastMessageBody.setText(messageToShow.getBody());
    mUnreadMessageIndicator.setVisibility(
        message.isUnread() ? View.VISIBLE : View.GONE);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    mInboxPresenter.showMessageContextMenu(menu, v, menuInfo, mMessage);
  }

  @OnClick({ R.id.last_message_layout })
  void onClick(View view) {
    view.showContextMenu();
  }

  @OnClick({ R.id.conversation_subject, R.id.collapsed_messages_layout })
  void showMessageView() {
    mInboxPresenter.showMessagePermalink(mMessage);
  }
}
