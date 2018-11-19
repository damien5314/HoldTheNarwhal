package com.ddiehl.android.htn.listings.links;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.View;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.listings.BaseListingsPresenter;
import com.ddiehl.android.htn.listings.subreddit.ThumbnailMode;
import com.ddiehl.android.htn.utils.ThemeUtilsKt;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.OnClick;
import rxreddit.model.Image;
import rxreddit.model.Link;

import static com.ddiehl.android.htn.listings.links.LinkOptionsBar.Icons.DOWNVOTE;
import static com.ddiehl.android.htn.listings.links.LinkOptionsBar.Icons.HIDE;
import static com.ddiehl.android.htn.listings.links.LinkOptionsBar.Icons.REPLY;
import static com.ddiehl.android.htn.listings.links.LinkOptionsBar.Icons.REPORT;
import static com.ddiehl.android.htn.listings.links.LinkOptionsBar.Icons.SAVE;
import static com.ddiehl.android.htn.listings.links.LinkOptionsBar.Icons.SHARE;
import static com.ddiehl.android.htn.listings.links.LinkOptionsBar.Icons.UPVOTE;


public class CommentsLinkViewHolder extends BaseLinkViewHolder {

    @BindView(R.id.link_options_bar)
    LinkOptionsBar linkOptionsBar;
    @BindView(R.id.link_parent_view)
    View parentLinkView;

    public CommentsLinkViewHolder(View view, LinkView linkView, BaseListingsPresenter presenter) {
        super(view, linkView, presenter);
        linkOptionsBar.showIcons(false, HIDE, SHARE);
        linkOptionsBar.setOnIconClickListener(REPLY, this::onReplyClicked);
        linkOptionsBar.setOnIconClickListener(UPVOTE, this::onUpvoteClicked);
        linkOptionsBar.setOnIconClickListener(DOWNVOTE, this::onDownvoteClicked);
        linkOptionsBar.setOnIconClickListener(SAVE, this::onSaveClicked);
        linkOptionsBar.setOnIconClickListener(HIDE, this::onHideClicked);
        linkOptionsBar.setOnIconClickListener(REPORT, this::onReportClicked);
    }

    void onReplyClicked() {
        linkPresenter.replyToLink(link);
    }

    void onUpvoteClicked() {
        linkPresenter.upvoteLink(link);
    }

    void onDownvoteClicked() {
        linkPresenter.downvoteLink(link);
    }

    void onSaveClicked() {
        if (link.isSaved()) {
            linkPresenter.unsaveLink(link);
        } else {
            linkPresenter.saveLink(link);
        }
    }

    void onHideClicked() {
        linkPresenter.hideLink(link);
    }

    void onReportClicked() {
        linkPresenter.reportLink(link);
    }

    @OnClick(R.id.link_comment_count)
    void showCommentsForLink() {
        // Already viewing the comments, do nothing
    }

    @Override
    protected void showLiked(@NotNull Link link) {
        // Determine tint color based on liked status and tint the buttons appropriately
        @ColorInt int color;
        if (link.isLiked() == null) {
            color = ThemeUtilsKt.getColorFromAttr(context, R.attr.textColorSecondary);
            linkOptionsBar.setVoted(0);
        } else if (link.isLiked()) {
            color = ThemeUtilsKt.getColorFromAttr(context, R.attr.contentLikedColor);
            linkOptionsBar.setVoted(1);
        } else {
            color = ThemeUtilsKt.getColorFromAttr(context, R.attr.contentDislikedColor);
            linkOptionsBar.setVoted(-1);
        }

        // Determine if we should show the score, or a placeholder if the score is hidden
        Integer score = link.getScore();
        String scoreStr = score == null ?
                context.getString(R.string.hidden_score_placeholder) : score.toString();

        // Format the number section of the score with a color span
        int length = scoreStr.length();
        int index = linkScore.getText().toString().indexOf(scoreStr);
        Spannable s = new SpannableString(linkScore.getText());
        s.setSpan(new ForegroundColorSpan(color), index, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the final stylized text
        linkScore.setText(s);
    }

    @Override
    protected void showThumbnail(@NotNull Link link, @NotNull ThumbnailMode mode) {
        // Nsfw/ThumbnailMode is inconsequential here, just show the best preview for screen size
        String url = null;
        if (link.getPreviewImages() != null) {
            List<Image> images = link.getPreviewImages();
            Image.Res image = images.get(0).getSource();
            url = image.getUrl();
            int height = image.getHeight();
            DisplayMetrics display = context.getResources().getDisplayMetrics();
            float scale = display.scaledDensity;
            float maxHeight = context.getResources().getDimension(R.dimen.link_image_full_size);
            linkThumbnail.getLayoutParams().height = (int) Math.min(maxHeight, height * scale);
            // FIXME Find a preview best for screen size
        }

        if (url == null) url = "";

        switch (url) {
            case "nsfw":
                linkThumbnail.setVisibility(View.GONE);
                break;
            case "":
            case "default":
            case "self":
                linkThumbnail.setVisibility(View.GONE);
                break;
            default:
                linkThumbnail.setVisibility(View.VISIBLE);
                loadThumbnail(url);
        }
    }

    @Override
    protected void showSaved(@NotNull Link link) {
        linkOptionsBar.setSaved(link.isSaved());
    }

    @Override
    protected void showParentLink(boolean visible) {
        parentLinkView.setVisibility(visible ? View.VISIBLE : View.GONE);
        parentLinkView.setOnClickListener(
                view -> linkPresenter.showCommentsForLink(link));
    }
}
