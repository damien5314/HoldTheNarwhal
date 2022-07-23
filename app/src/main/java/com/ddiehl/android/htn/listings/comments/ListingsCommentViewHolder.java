package com.ddiehl.android.htn.listings.comments;

import android.content.Context;
import android.text.Spanned;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.listings.BaseListingsPresenter;
import com.ddiehl.android.htn.utils.ThemeUtilsKt;
import com.ddiehl.android.htn.view.markdown.HtmlParser;
import com.ddiehl.timesincetextview.TimeSinceTextView;

import rxreddit.model.Comment;

public class ListingsCommentViewHolder extends RecyclerView.ViewHolder
        implements View.OnCreateContextMenuListener {

    private final Context context;
    private final BaseListingsPresenter commentPresenter;
    private final HtmlParser htmlParser;
    private Comment comment;

    private final TextView commentLinkTitleView;
    private final TextView commentLinkSubtitleView;
    private final TextView authorView;
    private final ViewGroup scoreViewLayout;
    private final TextView scoreView;
    private final TimeSinceTextView timestampView;
    private final View savedView;
    private final TextView bodyView;
    private final TextView gildedText;
    private final View controversialityIndicator;
    private final View commentMetadata;

    public ListingsCommentViewHolder(View view, BaseListingsPresenter presenter) {
        super(view);

        this.context = view.getContext();
        this.commentPresenter = presenter;
        this.htmlParser = new HtmlParser(context);

        commentLinkTitleView = view.findViewById(R.id.comment_link_title);
        commentLinkSubtitleView = view.findViewById(R.id.comment_link_subtitle);
        authorView = view.findViewById(R.id.comment_author);
        scoreViewLayout = view.findViewById(R.id.comment_score_layout);
        scoreView = view.findViewById(R.id.comment_score);
        timestampView = view.findViewById(R.id.comment_timestamp);
        savedView = view.findViewById(R.id.comment_saved_icon);
        bodyView = view.findViewById(R.id.comment_body);
        gildedText = view.findViewById(R.id.comment_gilded_text_view);
        controversialityIndicator = view.findViewById(R.id.comment_controversiality_indicator);
        commentMetadata = view.findViewById(R.id.comment_metadata);

        commentLinkTitleView.setOnClickListener(this::onClickTitle);
        commentMetadata.setOnClickListener(this::onClickMetadata);
        bodyView.setOnClickListener(this::onClickBody);

        itemView.setOnCreateContextMenuListener(this);
    }

    private void onClickTitle(View view) {
        commentPresenter.openCommentLink(comment);
    }

    private void onClickMetadata(View v) {
        v.showContextMenu();
    }

    private void onClickBody(View v) {
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
            String titleFormatter = context.getString(R.string.listing_comment_subtitle_format);
            commentLinkSubtitleView.setText(
                    String.format(titleFormatter, subreddit, linkAuthor));
        } else {
            String titleFormatter = context.getString(R.string.listing_comment_inbox_subtitle_format);
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
        return context.getString(resId);
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
                    final int opTextColor = ThemeUtilsKt.getColorFromAttr(context, R.attr.authorDecoratedTextColor);
                    authorView.setTextColor(opTextColor);
                    break;
                case "moderator":
                    authorView.setBackgroundResource(R.drawable.author_moderator_bg);
                    final int moderatorTextColor = ThemeUtilsKt.getColorFromAttr(context, R.attr.authorDecoratedTextColor);
                    authorView.setTextColor(moderatorTextColor);
                    break;
                case "admin":
                    authorView.setBackgroundResource(R.drawable.author_admin_bg);
                    final int adminTextColor = ThemeUtilsKt.getColorFromAttr(context, R.attr.authorDecoratedTextColor);
                    authorView.setTextColor(adminTextColor);
                    break;
                default:
            }
        } else {
            authorView.setBackgroundResource(0);
            final int defaultTextColor = ThemeUtilsKt.getColorFromAttr(context, R.attr.textColorSecondary);
            authorView.setTextColor(defaultTextColor);
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
                    String.format(context.getString(R.string.comment_score), comment.getScore()));
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
        if (comment.getLiked() == null) {
            scoreView.setTextColor(ThemeUtilsKt.getColorFromAttr(context, R.attr.textColorSecondary));
        } else if (comment.getLiked()) {
            final int upvoteColor = ThemeUtilsKt.getColorFromAttr(context, R.attr.contentLikedColor);
            scoreView.setTextColor(upvoteColor);
        } else {
            final int downvoteColor = ThemeUtilsKt.getColorFromAttr(context, R.attr.contentDislikedColor);
            scoreView.setTextColor(downvoteColor);
        }
    }

    private void showSaved(Comment comment) {
        savedView.setVisibility(comment.isSaved() ? View.VISIBLE : View.GONE);
    }

    private void showGilded(Comment comment) {
        Integer gilded = comment.getGilded();
        if (gilded != null && gilded > 0) {
            gildedText.setText(String.format(context.getString(R.string.link_gilded_text), gilded));
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
        CommentMenuHelper.showCommentContextMenu((FragmentActivity) view.getContext(), menu, comment);
    }
}
