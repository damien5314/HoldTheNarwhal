package com.ddiehl.android.htn.listings.comments;

import android.content.Context;
import android.text.Spanned;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.listings.BaseListingsPresenter;
import com.ddiehl.android.htn.view.markdown.HtmlParser;
import com.ddiehl.timesincetextview.TimeSinceTextView;

import javax.inject.Inject;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rxreddit.model.Comment;

public class ListingsCommentViewHolder extends RecyclerView.ViewHolder
        implements View.OnCreateContextMenuListener {

    @Inject Context appContext;
    @Inject HtmlParser htmlParser;
    private CommentView commentView;
    private BaseListingsPresenter commentPresenter;
    private Comment comment;

    @BindView(R.id.comment_link_title) TextView commentLinkTitleView;
    @BindView(R.id.comment_link_subtitle) TextView commentLinkSubtitleView;
    @BindView(R.id.comment_author) TextView authorView;
    @BindView(R.id.comment_score_layout) ViewGroup scoreViewLayout;
    @BindView(R.id.comment_score) TextView scoreView;
    @BindView(R.id.comment_timestamp) TimeSinceTextView timestampView;
    @BindView(R.id.comment_saved_icon) View savedView;
    @BindView(R.id.comment_body) TextView bodyView;
    @BindView(R.id.comment_gilded_text_view) TextView gildedText;
    @BindView(R.id.comment_controversiality_indicator) View controversialityIndicator;

    public ListingsCommentViewHolder(View view, CommentView commentView, BaseListingsPresenter presenter) {
        super(view);
        HoldTheNarwhal.getApplicationComponent().inject(this);
        this.commentView = commentView;
        commentPresenter = presenter;
        ButterKnife.bind(this, view);
        itemView.setOnCreateContextMenuListener(this);
    }

    @OnClick(R.id.comment_link_title)
    void onClickTitle() {
        commentPresenter.openCommentLink(comment);
    }

    @OnClick(R.id.comment_metadata)
    void onClickMetadata(View v) {
        v.showContextMenu();
    }

    @OnClick(R.id.comment_body)
    void onClickBody(View v) {
        v.showContextMenu();
    }

    public void bind(Comment comment, boolean showControversiality) {
        this.comment = comment;
        showLinkTitle(comment);
        showLinkSubtitle(comment);
        showAuthor(comment);
        showBody(comment);
        showScore(comment);
        showTimestamp(comment);
        showLiked(comment);
        showSaved(comment);
        showGilded(comment);
        showControversiality(comment, showControversiality);
    }

    private void showLinkTitle(Comment comment) {
        commentLinkTitleView.setText(comment.getLinkTitle());
    }

    private void showLinkSubtitle(Comment comment) {
        String linkAuthor = comment.getLinkAuthor();
        String subreddit = comment.getSubreddit();
        String subject = getSubjectAbbreviationForComment(comment);
        if (subject == null) {
            String titleFormatter = appContext.getString(R.string.listing_comment_subtitle_format);
            commentLinkSubtitleView.setText(
                    String.format(titleFormatter, subreddit, linkAuthor));
        } else {
            String titleFormatter = appContext.getString(R.string.listing_comment_inbox_subtitle_format);
            commentLinkSubtitleView.setText(
                    String.format(titleFormatter, subreddit, subject));
        }
    }

    private String getSubjectAbbreviationForComment(Comment comment) {
        String subject = comment.getSubject();
        if (subject == null) return null;
        int resId;
        if ("comment reply".equals(subject)) {
            resId = R.string.listing_comment_subject_commentreply;
        } else if ("post reply".equals(subject)) {
            resId = R.string.listing_comment_subject_postreply;
        } else { // if ("username mention".equals(subject)) {
            resId = R.string.listing_comment_subject_usernamemention;
        }
        return appContext.getString(resId);
    }

    private void showAuthor(Comment comment) {
        authorView.setVisibility(View.VISIBLE);
        String authorType = null;
        String distinguished = comment.getDistinguished();
        if (distinguished != null && !distinguished.equals("")) {
            authorType = distinguished;
        }
        if (authorType != null) {
            switch (authorType) {
                case "op":
                    authorView.setBackgroundResource(R.drawable.author_op_bg);
                    authorView.setTextColor(
                            ContextCompat.getColor(appContext, R.color.author_op_text));
                    break;
                case "moderator":
                    authorView.setBackgroundResource(R.drawable.author_moderator_bg);
                    authorView.setTextColor(
                            ContextCompat.getColor(appContext, R.color.author_moderator_text));
                    break;
                case "admin":
                    authorView.setBackgroundResource(R.drawable.author_admin_bg);
                    authorView.setTextColor(
                            ContextCompat.getColor(appContext, R.color.author_admin_text));
                    break;
                default:
            }
        } else {
            authorView.setBackgroundResource(0);
            authorView.setTextColor(
                    ContextCompat.getColor(appContext, R.color.secondary_text));
        }
        authorView.setText(comment.getAuthor());
    }

    private void showBody(Comment comment) {
        Spanned formatted = htmlParser.convert(comment.getBodyHtml());
        bodyView.setText(formatted);
    }

    private void showScore(Comment comment) {
        if (comment.getScore() == null) {
            scoreViewLayout.setVisibility(View.GONE);
        } else {
            scoreViewLayout.setVisibility(View.VISIBLE);
            scoreView.setText(
                    String.format(appContext.getString(R.string.comment_score), comment.getScore()));
        }
    }

    private void showTimestamp(Comment comment) {
        timestampView.setDate(comment.getCreateUtc().longValue());
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
        CharSequence text = timestampView.getText();
        timestampView.setText(edited ? text + "*" : text.toString().replace("*", ""));
    }

    // Set background tint based on isLiked
    private void showLiked(Comment comment) {
        if (comment.isLiked() == null) {
            scoreView.setTextColor(ContextCompat.getColor(appContext, R.color.secondary_text));
        } else if (comment.isLiked()) {
            scoreView.setTextColor(ContextCompat.getColor(appContext, R.color.reddit_orange_full));
        } else {
            scoreView.setTextColor(ContextCompat.getColor(appContext, R.color.reddit_blue_full));
        }
    }

    private void showSaved(Comment comment) {
        savedView.setVisibility(comment.isSaved() ? View.VISIBLE : View.GONE);
    }

    private void showGilded(Comment comment) {
        Integer gilded = comment.getGilded();
        if (gilded != null && gilded > 0) {
            gildedText.setText(String.format(appContext.getString(R.string.link_gilded_text), gilded));
            gildedText.setVisibility(View.VISIBLE);
        } else {
            gildedText.setVisibility(View.GONE);
        }
    }

    private void showControversiality(Comment comment, boolean showControversiality) {
        controversialityIndicator.setVisibility(showControversiality
                ? (comment.getControversiality() > 0 ? View.VISIBLE : View.GONE)
                : View.GONE);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        commentView.showCommentContextMenu(menu, view, comment);
    }
}
