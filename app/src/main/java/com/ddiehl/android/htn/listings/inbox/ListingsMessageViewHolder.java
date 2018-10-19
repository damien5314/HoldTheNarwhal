package com.ddiehl.android.htn.listings.inbox;

import android.content.Context;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.listings.BaseListingsPresenter;
import com.ddiehl.android.htn.utils.Utils;
import com.ddiehl.android.htn.view.markdown.HtmlParser;
import com.ddiehl.timesincetextview.TimeSince;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rxreddit.model.Listing;
import rxreddit.model.PrivateMessage;

public class ListingsMessageViewHolder extends RecyclerView.ViewHolder
        implements View.OnCreateContextMenuListener {

    @Inject protected Context appContext;
    @Inject HtmlParser htmlParser;

    private final PrivateMessageView privateMessageView;
    private final BaseListingsPresenter messagePresenter;
    private PrivateMessage message;

    @BindView(R.id.conversation_subject) TextView conversationSubject;
    @BindView(R.id.conversation_body_layout) ViewGroup conversationBodyLayout;
    @BindView(R.id.collapsed_messages_layout) ViewGroup collapsedMessagesLayout;
    @BindView(R.id.collapsed_messages_text) TextView collapsedMessagesText;
    @BindView(R.id.last_message_layout) ViewGroup lastMessageLayout;
    @BindView(R.id.message_indentation) View messageIndentation;
    @BindView(R.id.last_message_metadata) TextView lastMessageMetadata;
    @BindView(R.id.unread_message_indicator) View unreadMessageIndicator;
    @BindView(R.id.last_message_body) TextView lastMessageBody;

    public ListingsMessageViewHolder(View view, PrivateMessageView pmView, BaseListingsPresenter presenter) {
        super(view);
        HoldTheNarwhal.getApplicationComponent().inject(this);
        privateMessageView = pmView;
        messagePresenter = presenter;
        ButterKnife.bind(this, view);
        itemView.setOnCreateContextMenuListener(this);
    }

    public void bind(PrivateMessage message, boolean showReplies) {
        this.message = message;
        List<Listing> replies = null;
        if (message.getReplies() != null) {
            replies = message.getReplies().getData().getChildren();
        }
        PrivateMessage messageToShow;
        if (!showReplies) {
            ((View) conversationSubject.getParent()).setVisibility(View.GONE);
        }
        if (!showReplies || replies == null || replies.size() == 0) {
            messageToShow = this.message;
            // Collapse the "view more messages" view
            collapsedMessagesLayout.setVisibility(View.GONE);
            // Hide the indentation and remove background
            messageIndentation.setVisibility(View.GONE);
        } else {
            messageToShow = (PrivateMessage) replies.get(replies.size() - 1);
            // Expand the "view more messages" view
            collapsedMessagesLayout.setVisibility(View.VISIBLE);
            // Show the indentation and set background
            messageIndentation.setVisibility(View.VISIBLE);
        }
        // Show message metadata and text
        conversationSubject.setText(messageToShow.getSubject());
        if (replies != null) {
            int n = replies.size();
            collapsedMessagesText.setText(
                    appContext.getResources().getQuantityString(R.plurals.view_more_messages, n, n));
        }
        boolean isToMe = Utils.equals(
                messagePresenter.getUserIdentity().getName(), messageToShow.getDestination());
        String from = appContext.getString(
                isToMe ? R.string.message_metadata_from : R.string.message_metadata_to);
        from = String.format(from,
                isToMe ? messageToShow.getAuthor() : messageToShow.getDestination());
        String sent = appContext.getString(R.string.message_metadata_sent);
        sent = String.format(sent, TimeSince.getFormattedDateString(
                messageToShow.getCreatedUtc(), false, appContext));
        String text = from + " " + sent;
        lastMessageMetadata.setText(text);
        setTextToBody(lastMessageBody, messageToShow);
        lastMessageBody.setText(messageToShow.getBody());
        unreadMessageIndicator.setVisibility(
                message.isUnread() ? View.VISIBLE : View.GONE);
    }

    void setTextToBody(@NotNull TextView view, @NotNull PrivateMessage message) {
        view.setMovementMethod(LinkMovementMethod.getInstance());
        Spanned formatted = htmlParser.convert(message.getBodyHtml());
        view.setText(formatted);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        privateMessageView.showMessageContextMenu(menu, v, message);
    }

    @OnClick({ R.id.last_message_layout })
    void onClick(View view) {
        view.showContextMenu();
    }

    @OnClick({ R.id.conversation_subject, R.id.collapsed_messages_layout })
    void showMessageView() {
        messagePresenter.showMessagePermalink(message);
    }
}
