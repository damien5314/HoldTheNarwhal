package com.ddiehl.android.htn.listings.inbox;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.identity.IdentityManager;
import com.ddiehl.android.htn.utils.Utils;
import com.ddiehl.timesincetextview.TimeSince;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

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

    public static class VH extends RecyclerView.ViewHolder {

        @Inject protected Context mAppContext;
        @Inject protected IdentityManager mIdentityManager;

        @BindView(R.id.conversation_subject) protected TextView mConversationSubject;
        @BindView(R.id.conversation_body_layout) protected ViewGroup mConversationBodyLayout;
        @BindView(R.id.collapsed_messages_layout) protected ViewGroup mCollapsedMessagesLayout;
        @BindView(R.id.collapsed_messages_text) protected TextView mCollapsedMessagesText;
        @BindView(R.id.last_message_layout) protected ViewGroup mLastMessageLayout;
        @BindView(R.id.message_indentation) protected View mMessageIndentation;
        @BindView(R.id.last_message_metadata) protected TextView mLastMessageMetadata;
        @BindView(R.id.unread_message_indicator) protected View mUnreadMessageIndicator;
        @BindView(R.id.last_message_body) protected TextView mLastMessageBody;

        public VH(View view) {
            super(view);
            HoldTheNarwhal.getApplicationComponent().inject(this);
            ButterKnife.bind(this, view);
        }

        public void bind(PrivateMessage message) {
            ((View) mConversationSubject.getParent()).setVisibility(View.GONE);
            // Collapse the "view more messages" view
            mCollapsedMessagesLayout.setVisibility(View.GONE);
            // Hide the indentation and remove background
            mMessageIndentation.setVisibility(View.GONE);
            // Show message metadata and text
            mConversationSubject.setText(message.getSubject());
            boolean isToMe = Utils.equals(
                    mIdentityManager.getUserIdentity().getName(), message.getDestination());
            String from = mAppContext.getString(
                    isToMe ? R.string.message_metadata_from : R.string.message_metadata_to);
            from = String.format(from,
                    isToMe ? message.getAuthor() : message.getDestination());
            String sent = mAppContext.getString(R.string.message_metadata_sent);
            sent = String.format(sent, TimeSince.getFormattedDateString(
                    message.getCreatedUtc(), false, mAppContext));
            String text = from + " " + sent;
            mLastMessageMetadata.setText(text);
            mLastMessageBody.setText(message.getBody());
            mUnreadMessageIndicator.setVisibility(
                    message.isUnread() ? View.VISIBLE : View.GONE);
        }
    }
}
