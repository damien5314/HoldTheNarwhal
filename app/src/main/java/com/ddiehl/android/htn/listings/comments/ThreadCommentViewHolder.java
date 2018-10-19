package com.ddiehl.android.htn.listings.comments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.utils.ThemeUtilsKt;
import com.ddiehl.android.htn.view.ColorSwapTextView;
import com.ddiehl.android.htn.view.markdown.HtmlParser;
import com.ddiehl.timesincetextview.TimeSinceTextView;

import javax.inject.Inject;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rxreddit.model.Comment;
import rxreddit.model.Link;

public class ThreadCommentViewHolder extends RecyclerView.ViewHolder
        implements View.OnCreateContextMenuListener {

    private final Context context;
    private final LinkCommentsView linkCommentsView;
    private final LinkCommentsPresenter linkCommentsPresenter;
    private final HtmlParser htmlParser;
    private Comment comment;

    @BindView(R.id.comment_author) ColorSwapTextView authorView;
    @BindView(R.id.comment_score_layout) ViewGroup scoreViewLayout;
    @BindView(R.id.comment_score) TextView scoreView;
    @BindView(R.id.comment_timestamp) TimeSinceTextView timestampView;
    @BindView(R.id.comment_saved_icon) View savedView;
    @BindView(R.id.comment_body) TextView bodyView;
    @BindView(R.id.comment_gilded_text_view) TextView gildedText;
    @BindView(R.id.comment_controversiality_indicator) View controversialityIndicator;

    public ThreadCommentViewHolder(View view, LinkCommentsView linkCommentsView, LinkCommentsPresenter presenter) {
        super(view);

        this.context = view.getContext();
        this.linkCommentsView = linkCommentsView;
        this.linkCommentsPresenter = presenter;
        this.htmlParser = new HtmlParser(context);

        ButterKnife.bind(this, view);
        this.itemView.setOnCreateContextMenuListener(this);
    }

    @OnClick(R.id.comment_metadata)
    void onClickMetadata(View v) {
        linkCommentsPresenter.toggleThreadVisible(comment);
    }

    @OnClick(R.id.comment_body)
    void onClickBody(View v) {
        v.showContextMenu();
    }

    public void bind(final Link link, final Comment comment, boolean showControversiality) {
        this.comment = comment;
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
                * (int) context.getResources().getDimension(R.dimen.comment_indentation_margin);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        Configuration config = context.getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= 17 && config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            params.setMargins(0, 0, viewMargin, 0);
        } else {
            params.setMargins(viewMargin, 0, 0, 0);
        }
    }

    private void showAuthor(Link link, Comment comment) {
        String author = comment.getAuthor();
        authorView.setVisibility(View.VISIBLE);
        authorView.setText(author);
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
            //noinspection deprecation
            authorView.setBackgroundDrawable(authorView.getOriginalBackground());
            final int defaultTextColor = ThemeUtilsKt.getColorFromAttr(context, R.attr.textColorSecondary);
            authorView.setTextColor(defaultTextColor);
        }
    }

    private void showBody(Comment comment) {
        bodyView.setMovementMethod(LinkMovementMethod.getInstance());
        Spanned formatted = htmlParser.convert(comment.getBodyHtml());
        bodyView.setText(formatted);
    }

    private void showScore(Comment comment) {
        String text;
        if (comment.getScore() == null) {
            text = context.getString(R.string.hidden_score_placeholder);
        } else text = String.format(context.getString(R.string.comment_score), comment.getScore());
        scoreView.setText(text);
    }

    private void showTimestamp(Comment comment) {
        timestampView.setDate(comment.getCreateUtc().longValue());
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
        CharSequence text = timestampView.getText();
        timestampView.setText(edited ? text + "*" : text.toString().replace("*", ""));
    }

    private void setCollapsed(Comment comment) {
        bodyView.setVisibility(comment.isCollapsed() ? View.GONE : View.VISIBLE);
    }

    // Set background tint based on isLiked
    private void showLiked(Comment comment) {
        if (comment.isLiked() == null) {
            final int neutralTextColor = ThemeUtilsKt.getColorFromAttr(context, R.attr.textColorSecondary);
            scoreView.setTextColor(neutralTextColor);
        } else if (comment.isLiked()) {
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

    // Show gilding view if appropriate, else hide
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
        linkCommentsView.showCommentContextMenu(menu, view, comment);
    }
}
