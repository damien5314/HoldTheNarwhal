package com.ddiehl.android.htn.listings.inbox;

import android.content.Context;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
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

        @Inject IdentityManager identityManager;

        private final Context context;
        private final HtmlParser htmlParser;

        @BindView(R.id.conversation_subject) protected TextView conversationSubject;
        @BindView(R.id.conversation_body_layout) protected ViewGroup conversationBodyLayout;
        @BindView(R.id.collapsed_messages_layout) protected ViewGroup collapsedMessagesLayout;
        @BindView(R.id.collapsed_messages_text) protected TextView collapsedMessagesText;
        @BindView(R.id.last_message_layout) protected ViewGroup lastMessageLayout;
        @BindView(R.id.message_indentation) protected View messageIndentation;
        @BindView(R.id.last_message_metadata) protected TextView lastMessageMetadata;
        @BindView(R.id.unread_message_indicator) protected View unreadMessageIndicator;
        @BindView(R.id.last_message_body) protected TextView lastMessageBody;

        public VH(View view) {
            super(view);
            HoldTheNarwhal.getApplicationComponent().inject(this);

            context = view.getContext();
            htmlParser = new HtmlParser(context);

            ButterKnife.bind(this, view);
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
