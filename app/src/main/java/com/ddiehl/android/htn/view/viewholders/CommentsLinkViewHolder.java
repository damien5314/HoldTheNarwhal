package com.ddiehl.android.htn.view.viewholders;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.View;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.ThumbnailMode;
import com.ddiehl.android.htn.presenter.LinkPresenter;
import com.ddiehl.android.htn.view.LinkView;
import com.ddiehl.android.htn.view.widgets.LinkOptionsBar;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import rxreddit.model.Link;

import static com.ddiehl.android.htn.view.widgets.LinkOptionsBar.Icons.HIDE;

public class CommentsLinkViewHolder extends BaseLinkViewHolder {

    @BindView(R.id.link_options_bar)
    LinkOptionsBar mLinkOptionsBar;
    @BindView(R.id.link_parent_view)
    View mParentLinkView;

    public CommentsLinkViewHolder(View view, LinkView linkView, LinkPresenter presenter) {
        super(view, linkView, presenter);
        mLinkOptionsBar.showIcons(false, HIDE);
    }

    @OnClick(R.id.action_link_reply)
    void onReplyClicked() {
        mLinkPresenter.replyToLink(mLink);
    }

    @OnClick(R.id.action_link_upvote)
    void onUpvoteClicked() {
        mLinkPresenter.upvoteLink(mLink);
    }

    @OnClick(R.id.action_link_downvote)
    void onDownvoteClicked() {
        mLinkPresenter.downvoteLink(mLink);
    }

    @OnClick(R.id.action_link_save)
    void onSaveClicked() {
        if (mLink.isSaved()) {
            mLinkPresenter.unsaveLink(mLink);
        } else {
            mLinkPresenter.saveLink(mLink);
        }
    }

    @OnClick(R.id.action_link_share)
    void onShareClicked() {
        mLinkPresenter.shareLink(mLink);
    }

    @OnClick(R.id.action_link_hide)
    void onHideClicked() {
        mLinkPresenter.hideLink(mLink);
    }

    @OnClick(R.id.action_link_report)
    void onReportClicked() {
        mLinkPresenter.reportLink(mLink);
    }

    @OnClick(R.id.link_comment_count)
    void showCommentsForLink() {
        // Already viewing the comments, do nothing
    }

    @Override
    protected void showLiked(@NonNull Link link) {
        // Determine tint color based on liked status and tint the buttons appropriately
        @ColorInt int color;
        if (link.isLiked() == null) {
            color = ContextCompat.getColor(mContext, R.color.secondary_text);
            mLinkOptionsBar.setVoted(0);
        } else if (link.isLiked()) {
            color = ContextCompat.getColor(mContext, R.color.reddit_orange_full);
            mLinkOptionsBar.setVoted(1);
        } else {
            color = ContextCompat.getColor(mContext, R.color.reddit_blue_full);
            mLinkOptionsBar.setVoted(-1);
        }

        // Determine if we should show the score, or a placeholder if the score is hidden
        Integer score = link.getScore();
        String scoreStr = score == null ?
                mContext.getString(R.string.hidden_score_placeholder) : score.toString();

        // Format the number section of the score with a color span
        int length = scoreStr.length();
        int index = mLinkScore.getText().toString().indexOf(scoreStr);
        Spannable s = new SpannableString(mLinkScore.getText());
        s.setSpan(new ForegroundColorSpan(color), index, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the final stylized text
        mLinkScore.setText(s);
    }

    @Override
    protected void showThumbnail(@NonNull Link link, @NonNull ThumbnailMode mode) {
        // Nsfw/ThumbnailMode is inconsequential here, just show the best preview for screen size
        String url = null;
        if (link.getPreviewImages() != null) {
            List<Link.Preview.Image> images = link.getPreviewImages();
            Link.Preview.Image.Res image = images.get(0).getSource();
            url = image.getUrl();
            int height = image.getHeight();
            DisplayMetrics display = mContext.getResources().getDisplayMetrics();
            float scale = display.scaledDensity;
            float maxHeight = mContext.getResources().getDimension(R.dimen.link_image_full_size);
            mLinkThumbnail.getLayoutParams().height = (int) Math.min(maxHeight, height * scale);
            // FIXME Find a preview best for screen size
        }

        if (url == null) url = "";

        switch (url) {
            case "nsfw":
                mLinkThumbnail.setVisibility(View.GONE);
                break;
            case "":
            case "default":
            case "self":
                mLinkThumbnail.setVisibility(View.GONE);
                break;
            default:
                mLinkThumbnail.setVisibility(View.VISIBLE);
                loadThumbnail(url);
        }
    }

    @Override
    protected void showSaved(@NonNull Link link) {
        mLinkOptionsBar.setSaved(link.isSaved());
    }

    @Override
    protected void showParentLink(boolean visible) {
        mParentLinkView.setVisibility(visible ? View.VISIBLE : View.GONE);
        mParentLinkView.setOnClickListener(
                view -> mLinkPresenter.showCommentsForLink(mLink));
    }
}
