package com.ddiehl.android.htn.listings.inbox;

import android.content.Context;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.listings.BaseListingsPresenter;
import com.ddiehl.android.htn.utils.Utils;
import com.ddiehl.android.htn.view.markdown.HtmlParser;
import com.ddiehl.timesincetextview.TimeSince;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import rxreddit.model.Listing;
import rxreddit.model.PrivateMessage;

public class ListingsMessageViewHolder extends RecyclerView.ViewHolder
        implements View.OnCreateContextMenuListener {

    private final Context context;
    private final BaseListingsPresenter messagePresenter;
    private final HtmlParser htmlParser;
    private PrivateMessage message;

    private final TextView conversationSubject;
    private final ViewGroup conversationBodyLayout;
    private final ViewGroup collapsedMessagesLayout;
    private final TextView collapsedMessagesText;
    private final ViewGroup lastMessageLayout;
    private final View messageIndentation;
    private final TextView lastMessageMetadata;
    private final View unreadMessageIndicator;
    private final TextView lastMessageBody;

    public ListingsMessageViewHolder(View view, BaseListingsPresenter presenter) {
        super(view);

        context = view.getContext();
        messagePresenter = presenter;
        htmlParser = new HtmlParser(context);

        conversationSubject = view.findViewById(R.id.conversation_subject);
        conversationBodyLayout = view.findViewById(R.id.conversation_body_layout);
        collapsedMessagesLayout = view.findViewById(R.id.collapsed_messages_layout);
        collapsedMessagesText = view.findViewById(R.id.collapsed_messages_text);
        lastMessageLayout = view.findViewById(R.id.last_message_layout);
        messageIndentation = view.findViewById(R.id.message_indentation);
        lastMessageMetadata = view.findViewById(R.id.last_message_metadata);
        unreadMessageIndicator = view.findViewById(R.id.unread_message_indicator);
        lastMessageBody = view.findViewById(R.id.last_message_body);

        lastMessageLayout.setOnClickListener(this::onClick);
        conversationSubject.setOnClickListener(this::showMessageView);
        collapsedMessagesLayout.setOnClickListener(this::showMessageView);

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
                    context.getResources().getQuantityString(R.plurals.view_more_messages, n, n));
        }
        boolean isToMe = Utils.equals(
                messagePresenter.getUserIdentity().getName(), messageToShow.getDestination());
        String from = context.getString(
                isToMe ? R.string.message_metadata_from : R.string.message_metadata_to);
        from = String.format(from,
                isToMe ? messageToShow.getAuthor() : messageToShow.getDestination());
        String sent = context.getString(R.string.message_metadata_sent);
        sent = String.format(sent, TimeSince.getFormattedDateString(
                messageToShow.getCreatedUtc(), false, context));
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
        InboxMenuHelper.INSTANCE.showMessageContextMenu(((FragmentActivity) v.getContext()), menu, message);
        messagePresenter.onContextMenuShownForMessage(message);
    }

    private void onClick(View view) {
        view.showContextMenu();
    }

    private void showMessageView(View view) {
        messagePresenter.showMessagePermalink(message);
    }
}
