package com.ddiehl.android.htn.listings.comments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.ColorSwapTextView;
import com.ddiehl.android.htn.view.markdown.HtmlParser;
import com.ddiehl.android.htn.view.markdown.MarkdownParser;
import com.ddiehl.timesincetextview.TimeSinceTextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rxreddit.model.Comment;
import rxreddit.model.Link;

public class ThreadCommentViewHolder extends RecyclerView.ViewHolder
        implements View.OnCreateContextMenuListener {

    @Inject Context mContext;
    @Inject @Nullable MarkdownParser mMarkdownParser;
    @Inject HtmlParser mHtmlParser;

    private final LinkCommentsView mLinkCommentsView;
    private final LinkCommentsPresenter mLinkCommentsPresenter;
    private Comment mComment;

    @BindView(R.id.comment_author) ColorSwapTextView mAuthorView;
    @BindView(R.id.comment_score_layout) ViewGroup mScoreViewLayout;
    @BindView(R.id.comment_score) TextView mScoreView;
    @BindView(R.id.comment_timestamp) TimeSinceTextView mTimestampView;
    @BindView(R.id.comment_saved_icon) View mSavedView;
    @BindView(R.id.comment_body) TextView mBodyView;
    @BindView(R.id.comment_gilded_text_view) TextView mGildedText;
    @BindView(R.id.comment_controversiality_indicator) View mControversialityIndicator;

    public ThreadCommentViewHolder(View view, LinkCommentsView linkCommentsView, LinkCommentsPresenter presenter) {
        super(view);
        HoldTheNarwhal.getApplicationComponent().inject(this);
        mLinkCommentsView = linkCommentsView;
        mLinkCommentsPresenter = presenter;
        ButterKnife.bind(this, view);
        itemView.setOnCreateContextMenuListener(this);
    }

    @OnClick(R.id.comment_metadata)
    void onClickMetadata(View v) {
        mLinkCommentsPresenter.toggleThreadVisible(mComment);
    }

    @OnClick(R.id.comment_body)
    void onClickBody(View v) {
        v.showContextMenu();
    }

    public void bind(final Link link, final Comment comment, boolean showControversiality) {
        mComment = comment;
        addPaddingViews(comment);
        showAuthor(link, comment);
        showBody(comment);
        showScore(comment);
        showTimestamp(comment);
        showEdited(comment);
        setCollapsed(comment);
        showLiked(comment);
        showSaved(comment);
        showGilded(comment);
        showControversiality(comment, showControversiality);
    }

    public void bind(final Comment comment, boolean showControversiality) {
        bind(null, comment, showControversiality);
    }

    // Add padding views to indentation_wrapper based on depth of comment
    private void addPaddingViews(Comment comment) {
        int viewMargin = (comment.getDepth() - 2)
                * (int) mContext.getResources().getDimension(R.dimen.comment_indentation_margin);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        Configuration config = mContext.getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= 17 && config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            params.setMargins(0, 0, viewMargin, 0);
        } else {
            params.setMargins(viewMargin, 0, 0, 0);
        }
    }

    private void showAuthor(Link link, Comment comment) {
        String author = comment.getAuthor();
        mAuthorView.setVisibility(View.VISIBLE);
        mAuthorView.setText(author);
        String authorType = null;
        String distinguished = comment.getDistinguished();
        if (distinguished != null && !distinguished.equals("")) {
            authorType = distinguished;
        }
        if (link != null && author.equals(link.getAuthor())) {
            authorType = "op";
        }
        if (authorType != null) {
            switch (authorType) {
                case "op":
                    mAuthorView.setBackgroundResource(R.drawable.author_op_bg);
                    mAuthorView.setTextColor(ContextCompat.getColor(mContext, R.color.author_op_text));
                    break;
                case "moderator":
                    mAuthorView.setBackgroundResource(R.drawable.author_moderator_bg);
                    mAuthorView.setTextColor(ContextCompat.getColor(mContext, R.color.author_moderator_text));
                    break;
                case "admin":
                    mAuthorView.setBackgroundResource(R.drawable.author_admin_bg);
                    mAuthorView.setTextColor(ContextCompat.getColor(mContext, R.color.author_admin_text));
                    break;
                default:
            }
        } else {
            //noinspection deprecation
            mAuthorView.setBackgroundDrawable(mAuthorView.getOriginalBackground());
            mAuthorView.setTextColor(ContextCompat.getColor(mContext, R.color.secondary_text));
        }
    }

    private void showBody(Comment comment) {
        mBodyView.setMovementMethod(LinkMovementMethod.getInstance());
        if (mMarkdownParser != null) {
            CharSequence formatted = mMarkdownParser.convert(comment.getBody());
            mBodyView.setText(formatted);
        } else {
            Spanned formatted = mHtmlParser.convert(comment.getBodyHtml());
            mBodyView.setText(formatted);
        }
    }

    private void showScore(Comment comment) {
        String text;
        if (comment.getScore() == null) {
            text = mContext.getString(R.string.hidden_score_placeholder);
        } else text = String.format(mContext.getString(R.string.comment_score), comment.getScore());
        mScoreView.setText(text);
    }

    private void showTimestamp(Comment comment) {
        mTimestampView.setDate(comment.getCreateUtc().longValue());
    }

    private void showEdited(Comment comment) {
        if (comment.isEdited() != null) {
            switch (comment.isEdited()) {
                case "":
                case "0":
                case "false":
                    setEdited(false);
                    break;
                default:
                    setEdited(true);
            }
        }
    }

    private void setEdited(boolean edited) {
        CharSequence text = mTimestampView.getText();
        mTimestampView.setText(edited ? text + "*" : text.toString().replace("*", ""));
    }

    private void setCollapsed(Comment comment) {
        mBodyView.setVisibility(comment.isCollapsed() ? View.GONE : View.VISIBLE);
    }

    // Set background tint based on isLiked
    private void showLiked(Comment comment) {
        if (comment.isLiked() == null) {
            mScoreView.setTextColor(ContextCompat.getColor(mContext, R.color.secondary_text));
        } else if (comment.isLiked()) {
            mScoreView.setTextColor(ContextCompat.getColor(mContext, R.color.reddit_orange_full));
        } else {
            mScoreView.setTextColor(ContextCompat.getColor(mContext, R.color.reddit_blue_full));
        }
    }

    private void showSaved(Comment comment) {
        mSavedView.setVisibility(comment.isSaved() ? View.VISIBLE : View.GONE);
    }

    // Show gilding view if appropriate, else hide
    private void showGilded(Comment comment) {
        Integer gilded = comment.getGilded();
        if (gilded != null && gilded > 0) {
            mGildedText.setText(String.format(mContext.getString(R.string.link_gilded_text), gilded));
            mGildedText.setVisibility(View.VISIBLE);
        } else {
            mGildedText.setVisibility(View.GONE);
        }
    }

    private void showControversiality(Comment comment, boolean showControversiality) {
        mControversialityIndicator.setVisibility(showControversiality
                ? (comment.getControversiality() > 0 ? View.VISIBLE : View.GONE)
                : View.GONE);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        mLinkCommentsView.showCommentContextMenu(menu, view, mComment);
    }
}
