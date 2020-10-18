package com.ddiehl.android.htn.listings.links;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.RequiresApi;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.utils.ThemeUtilsKt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LinkOptionsBar extends LinearLayout {

    static final @LayoutRes
    int LAYOUT_RES_ID = R.layout.link_options_bar;

    public enum Icons {
        REPLY, UPVOTE, DOWNVOTE, SAVE, SHARE, HIDE, REPORT
    }

    private View linkReply;
    private ImageView linkReplyIcon;
    private View linkUpvote;
    private ImageView linkUpvoteIcon;
    private View linkDownvote;
    private ImageView linkDownvoteIcon;
    private View linkSave;
    private ImageView linkSaveIcon;
    private View linkShare;
    private ImageView linkShareIcon;
    private View linkHide;
    private ImageView linkHideIcon;
    private View linkReport;
    private ImageView linkReportIcon;

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
        linkReply = findViewById(R.id.action_link_reply);
        linkReplyIcon = findViewById(R.id.action_link_reply_icon);
        linkUpvote = findViewById(R.id.action_link_upvote);
        linkUpvoteIcon = findViewById(R.id.action_link_upvote_icon);
        linkDownvote = findViewById(R.id.action_link_downvote);
        linkDownvoteIcon = findViewById(R.id.action_link_downvote_icon);
        linkSave = findViewById(R.id.action_link_save);
        linkSaveIcon = findViewById(R.id.action_link_save_icon);
        linkShare = findViewById(R.id.action_link_share);
        linkShareIcon = findViewById(R.id.action_link_share_icon);
        linkHide = findViewById(R.id.action_link_hide);
        linkHideIcon = findViewById(R.id.action_link_hide_icon);
        linkReport = findViewById(R.id.action_link_report);
        linkReportIcon = findViewById(R.id.action_link_report_icon);
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
            final int upvoteColor = ThemeUtilsKt.getColorFromAttr(getContext(), R.attr.contentLikedColor);
            linkUpvoteIcon.setColorFilter(upvoteColor);
        } else if (voted == -1) {
            final int downvoteColor = ThemeUtilsKt.getColorFromAttr(getContext(), R.attr.contentDislikedColor);
            linkDownvoteIcon.setColorFilter(downvoteColor);
        }
    }

    public void setSaved(boolean saved) {
        if (saved) {
            final int activatedOptionColor =
                    ThemeUtilsKt.getColorFromAttr(getContext(), R.attr.optionsBarActivatedColor);
            linkSaveIcon.setColorFilter(activatedOptionColor);
        } else {
            linkSaveIcon.setColorFilter(null);
        }
    }
}
