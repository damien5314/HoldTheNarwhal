package com.ddiehl.android.htn.listings.links;

import android.content.Context;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ddiehl.android.htn.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LinkOptionsBar extends LinearLayout {

    static final @LayoutRes int LAYOUT_RES_ID = R.layout.link_options_bar;

    public enum Icons {
        REPLY, UPVOTE, DOWNVOTE, SAVE, SHARE, HIDE, REPORT
    }

    @BindView(R.id.action_link_reply)
    View linkReply;
    @BindView(R.id.action_link_reply_icon)
    ImageView linkReplyIcon;

    @BindView(R.id.action_link_upvote)
    View linkUpvote;
    @BindView(R.id.action_link_upvote_icon)
    ImageView linkUpvoteIcon;

    @BindView(R.id.action_link_downvote)
    View linkDownvote;
    @BindView(R.id.action_link_downvote_icon)
    ImageView linkDownvoteIcon;

    @BindView(R.id.action_link_save)
    View linkSave;
    @BindView(R.id.action_link_save_icon)
    ImageView linkSaveIcon;

    @BindView(R.id.action_link_share)
    View linkShare;
    @BindView(R.id.action_link_share_icon)
    ImageView linkShareIcon;

    @BindView(R.id.action_link_hide)
    View linkHide;
    @BindView(R.id.action_link_hide_icon)
    ImageView linkHideIcon;

    @BindView(R.id.action_link_report)
    View linkReport;
    @BindView(R.id.action_link_report_icon)
    ImageView linkReportIcon;

    @Nullable Integer voted;

    public LinkOptionsBar(Context context) {
        this(context, null);
    }

    public LinkOptionsBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinkOptionsBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LinkOptionsBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        LayoutInflater.from(context)
                .inflate(LAYOUT_RES_ID, this);
        ButterKnife.bind(this);
    }

    public void showIcons(boolean show, Icons icon, Icons... icons) {
        showIconHelper(show, icon);
        if (icons != null) {
            for (Icons i : icons) {
                showIconHelper(show, i);
            }
        }
    }

    void showIconHelper(boolean show, Icons icon) {
        switch (icon) {
            case REPLY:
                linkReply.setVisibility(show ? VISIBLE : GONE);
                break;
            case UPVOTE:
                linkUpvote.setVisibility(show ? VISIBLE : GONE);
                break;
            case DOWNVOTE:
                linkDownvote.setVisibility(show ? VISIBLE : GONE);
                break;
            case SAVE:
                linkSave.setVisibility(show ? VISIBLE : GONE);
                break;
            case SHARE:
                linkShare.setVisibility(show ? VISIBLE : GONE);
                break;
            case HIDE:
                linkHide.setVisibility(show ? VISIBLE : GONE);
                break;
            case REPORT:
                linkReport.setVisibility(show ? VISIBLE : GONE);
                break;
        }
    }

    public void setOnIconClickListener(Icons icon, Runnable callback) {
        switch (icon) {
            case REPLY:
                linkReply.setOnClickListener((view) -> callback.run());
                break;
            case UPVOTE:
                linkUpvote.setOnClickListener((view) -> callback.run());
                break;
            case DOWNVOTE:
                linkDownvote.setOnClickListener((view) -> callback.run());
                break;
            case SAVE:
                linkSave.setOnClickListener((view) -> callback.run());
                break;
            case SHARE:
                linkShare.setOnClickListener((view) -> callback.run());
                break;
            case HIDE:
                linkHide.setOnClickListener((view) -> callback.run());
                break;
            case REPORT:
                linkReport.setOnClickListener((view) -> callback.run());
                break;
        }
    }

    public void setVoted(@NotNull Integer voted) {
        this.voted = voted;

        // Determine tint color based on liked status and tint the buttons appropriately
        linkUpvoteIcon.setColorFilter(null);
        linkDownvoteIcon.setColorFilter(null);

        if (voted == 1) {
            linkUpvoteIcon.setColorFilter(
                    ContextCompat.getColor(getContext(), R.color.reddit_orange_full)
            );
        } else if (voted == -1) {
            linkDownvoteIcon.setColorFilter(
                    ContextCompat.getColor(getContext(), R.color.reddit_blue_full)
            );
        }
    }

    public void setSaved(boolean saved) {
        if (saved) {
            linkSaveIcon.setColorFilter(
                    ContextCompat.getColor(getContext(), R.color.link_saved_color)
            );
        } else {
            linkSaveIcon.setColorFilter(null);
        }
    }
}
