package com.ddiehl.android.htn.listings.inbox;

import android.content.Context;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.identity.IdentityManager;
import com.ddiehl.android.htn.utils.Utils;
import com.ddiehl.android.htn.view.markdown.HtmlParser;
import com.ddiehl.timesincetextview.TimeSince;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rxreddit.model.PrivateMessage;

public class PrivateMessageAdapter extends RecyclerView.Adapter<PrivateMessageAdapter.VH> {

    private final List<PrivateMessage> mMessages = new ArrayList<>();

    public List<PrivateMessage> getMessages() {
        return mMessages;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listings_message_nocard, parent, false);
        return new VH(view);
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        PrivateMessage message = mMessages.get(position);
        holder.bind(message);
    }

    public static final class VH extends RecyclerView.ViewHolder {

        @Inject
        IdentityManager identityManager;

        private final Context context;
        private final HtmlParser htmlParser;

        private final TextView conversationSubject;
        private final ViewGroup conversationBodyLayout;
        private final ViewGroup collapsedMessagesLayout;
        private final TextView collapsedMessagesText;
        private final ViewGroup lastMessageLayout;
        private final View messageIndentation;
        private final TextView lastMessageMetadata;
        private final View unreadMessageIndicator;
        private final TextView lastMessageBody;

        public VH(View view) {
            super(view);
            HoldTheNarwhal.getApplicationComponent().inject(this);

            context = view.getContext();
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
        }

        public void bind(PrivateMessage message) {
            // Hide conversation subject
            ((View) conversationSubject.getParent())
                    .setVisibility(View.GONE);

            // Collapse the "view more messages" view
            collapsedMessagesLayout.setVisibility(View.GONE);

            // Hide the indentation and remove background
            messageIndentation.setVisibility(View.GONE);

            // Show message metadata and text
            conversationSubject.setText(message.getSubject());
            boolean isToMe = Utils.equals(
                    identityManager.getUserIdentity().getName(), message.getDestination());

            // Build 'from' text
            String from = context.getString(
                    isToMe ? R.string.message_metadata_from : R.string.message_metadata_to
            );
            from = String.format(from,
                    isToMe ? message.getAuthor() : message.getDestination());

            // Build 'sent' text
            String sent = context.getString(R.string.message_metadata_sent);
            sent = String.format(sent, TimeSince.getFormattedDateString(
                    message.getCreatedUtc(), false, context));

            // Set message metadata
            String text = from + " " + sent;
            lastMessageMetadata.setText(text);

            // Set text for message body
            setTextToBody(lastMessageBody, message);

            // Show/hide unread message indicator
            unreadMessageIndicator.setVisibility(
                    message.isUnread() ? View.VISIBLE : View.GONE
            );
        }

        void setTextToBody(@NotNull TextView view, @NotNull PrivateMessage message) {
            view.setMovementMethod(LinkMovementMethod.getInstance());
            Spanned formatted = htmlParser.convert(message.getBodyHtml());
            view.setText(formatted);
        }
    }
}
