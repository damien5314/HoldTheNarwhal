package com.ddiehl.android.htn.listings.links;

import android.content.Context;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ddiehl.android.htn.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LinkOptionsBar extends LinearLayout {

    static final @LayoutRes int LAYOUT_RES_ID = R.layout.link_options_bar;

    public enum Icons {
        REPLY, UPVOTE, DOWNVOTE, SAVE, SHARE, HIDE, REPORT
    }

    @BindView(R.id.action_link_reply)
    View mLinkReply;
    @BindView(R.id.action_link_reply_icon)
    ImageView mLinkReplyIcon;

    @BindView(R.id.action_link_upvote)
    View mLinkUpvote;
    @BindView(R.id.action_link_upvote_icon)
    ImageView mLinkUpvoteIcon;

    @BindView(R.id.action_link_downvote)
    View mLinkDownvote;
    @BindView(R.id.action_link_downvote_icon)
    ImageView mLinkDownvoteIcon;

    @BindView(R.id.action_link_save)
    View mLinkSave;
    @BindView(R.id.action_link_save_icon)
    ImageView mLinkSaveIcon;

    @BindView(R.id.action_link_share)
    View mLinkShare;
    @BindView(R.id.action_link_share_icon)
    ImageView mLinkShareIcon;

    @BindView(R.id.action_link_hide)
    View mLinkHide;
    @BindView(R.id.action_link_hide_icon)
    ImageView mLinkHideIcon;

    @BindView(R.id.action_link_report)
    View mLinkReport;
    @BindView(R.id.action_link_report_icon)
    ImageView mLinkReportIcon;

    @Nullable Integer mVoted;

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
                mLinkReply.setVisibility(show ? VISIBLE : GONE);
                break;
            case UPVOTE:
                mLinkUpvote.setVisibility(show ? VISIBLE : GONE);
                break;
            case DOWNVOTE:
                mLinkDownvote.setVisibility(show ? VISIBLE : GONE);
                break;
            case SAVE:
                mLinkSave.setVisibility(show ? VISIBLE : GONE);
                break;
            case SHARE:
                mLinkShare.setVisibility(show ? VISIBLE : GONE);
                break;
            case HIDE:
                mLinkHide.setVisibility(show ? VISIBLE : GONE);
                break;
            case REPORT:
                mLinkReport.setVisibility(show ? VISIBLE : GONE);
                break;
        }
    }

    public void setOnIconClickListener(Icons icon, Runnable callback) {
        switch (icon) {
            case REPLY:
                mLinkReply.setOnClickListener((view) -> callback.run());
                break;
            case UPVOTE:
                mLinkUpvote.setOnClickListener((view) -> callback.run());
                break;
            case DOWNVOTE:
                mLinkDownvote.setOnClickListener((view) -> callback.run());
                break;
            case SAVE:
                mLinkSave.setOnClickListener((view) -> callback.run());
                break;
            case SHARE:
                mLinkShare.setOnClickListener((view) -> callback.run());
                break;
            case HIDE:
                mLinkHide.setOnClickListener((view) -> callback.run());
                break;
            case REPORT:
                mLinkReport.setOnClickListener((view) -> callback.run());
                break;
        }
    }

    public void setVoted(@NonNull Integer voted) {
        mVoted = voted;

        // Determine tint color based on liked status and tint the buttons appropriately
        mLinkUpvoteIcon.setColorFilter(null);
        mLinkDownvoteIcon.setColorFilter(null);

        if (voted == 1) {
            mLinkUpvoteIcon.setColorFilter(
                    ContextCompat.getColor(getContext(), R.color.reddit_orange_full)
            );
        } else if (voted == -1) {
            mLinkDownvoteIcon.setColorFilter(
                    ContextCompat.getColor(getContext(), R.color.reddit_blue_full)
            );
        }
    }

    public void setSaved(boolean saved) {
        if (saved) {
            mLinkSaveIcon.setColorFilter(
                    ContextCompat.getColor(getContext(), R.color.link_saved_color)
            );
        } else {
            mLinkSaveIcon.setColorFilter(null);
        }
    }
}
