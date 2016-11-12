package com.ddiehl.android.htn.view.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.ThumbnailMode;
import com.ddiehl.android.htn.presenter.LinkPresenter;
import com.ddiehl.android.htn.view.LinkView;
import com.ddiehl.android.htn.view.widgets.ColorSwapTextView;
import com.ddiehl.timesincetextview.TimeSinceTextView;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rxreddit.model.Link;

public abstract class BaseLinkViewHolder extends RecyclerView.ViewHolder
        implements View.OnCreateContextMenuListener {

    protected final Context mContext;
    protected final LinkView mLinkView;
    protected final LinkPresenter mLinkPresenter;
    protected Link mLink;

    @BindView(R.id.link_view) protected View mView;
    @BindView(R.id.link_saved_view) protected View mSavedView;
    @BindView(R.id.link_title) protected TextView mLinkTitle;
    @BindView(R.id.link_domain) protected TextView mLinkDomain;
    @BindView(R.id.link_score) protected TextView mLinkScore;
    @BindView(R.id.link_author) protected ColorSwapTextView mLinkAuthor;
    @BindView(R.id.link_subreddit) protected TextView mLinkSubreddit;
    @BindView(R.id.link_comment_count) protected TextView mLinkComments;
    @BindView(R.id.link_self_text) protected TextView mSelfText;
    @BindView(R.id.link_nsfw_indicator) protected TextView mNsfwIndicator;
    @BindView(R.id.link_thumbnail) protected ImageView mLinkThumbnail;
    @BindView(R.id.link_timestamp) protected TimeSinceTextView mLinkTimestamp;
    @BindView(R.id.link_gilded_text_view) protected TextView mGildedText;
    @BindView(R.id.link_stickied_view) protected View mStickiedView;

    public BaseLinkViewHolder(View view, LinkView linkView, LinkPresenter presenter) {
        super(view);
        mContext = view.getContext().getApplicationContext();
        mLinkView = linkView;
        mLinkPresenter = presenter;
        ButterKnife.bind(this, view);
        itemView.setOnCreateContextMenuListener(this);
    }

    @OnClick({ R.id.link_view, R.id.link_thumbnail })
    void openLink() {
        mLinkPresenter.openLink(mLink);
    }

    @OnClick(R.id.link_metadata)
    void showContextMenu(View v) {
        v.showContextMenu();
    }

    public void bind(
            @NonNull Link link, boolean showSelfText, ThumbnailMode mode,
            boolean showNsfw, boolean showParentLink) {
        mLink = link;
        showSelfText(link, showSelfText);
        showScore(link);
        showTitle(link);
        showAuthor(link);
        showTimestamp(link);
        showSubreddit(link);
        showDomain(link);
        showCommentCount(link);
        showNsfwTag(link.getOver18() && showNsfw);
        showThumbnail(link, mode);
        showLiked(link);
        showGilded(link);
        showSaved(link);
        showStickied(link);
        showParentLink(showParentLink);
    }

    protected void showSelfText(@NonNull Link link, boolean showSelfText) {
//    mLinkView.setVisibility(View.VISIBLE);
        if (link.getSelftext() != null && !link.getSelftext().equals("") && showSelfText) {
            mSelfText.setText(link.getSelftext());
            mSelfText.setVisibility(View.VISIBLE);
        } else {
            mSelfText.setVisibility(View.GONE);
        }
    }

    protected void showScore(@NonNull Link link) {
        Integer score = link.getScore();
        if (score == null) {
            mLinkScore.setText(
                    mContext.getString(R.string.hidden_score_placeholder));
        } else {
            mLinkScore.setText(
                    mContext.getResources().getQuantityString(R.plurals.link_score, score, score));
        }
    }

    protected void showTitle(@NonNull Link link) {
        mLinkTitle.setText(link.getTitle());
    }

    protected void showAuthor(@NonNull Link link) {
        mLinkAuthor.setText(
                String.format(mContext.getString(R.string.link_author), link.getAuthor()));
        String distinguished = link.getDistinguished();
        if (distinguished == null || distinguished.equals("")) {
            // noinspection deprecation
            mLinkAuthor.setBackgroundDrawable(mLinkAuthor.getOriginalBackground());
            mLinkAuthor.setTextColor(mLinkAuthor.getOriginalTextColor());
        } else {
            switch (distinguished) {
                case "moderator":
                    mLinkAuthor.setBackgroundResource(R.drawable.author_moderator_bg);
                    mLinkAuthor.setTextColor(
                            ContextCompat.getColor(mContext, R.color.author_moderator_text));
                    break;
                case "admin":
                    mLinkAuthor.setBackgroundResource(R.drawable.author_admin_bg);
                    mLinkAuthor.setTextColor(
                            ContextCompat.getColor(mContext, R.color.author_admin_text));
                    break;
                default:
            }
        }
    }

    protected void showTimestamp(@NonNull Link link) {
        long timestamp = link.getCreatedUtc().longValue();
        mLinkTimestamp.setDate(timestamp);
        if (link.isEdited() != null) {
            switch (link.isEdited()) {
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
        CharSequence text = mLinkTimestamp.getText();
        mLinkTimestamp.setText(edited ? text + "*" : text.toString().replace("*", ""));
    }

    protected void showSubreddit(@NonNull Link link) {
        mLinkSubreddit.setText(
                String.format(mContext.getString(R.string.link_subreddit), link.getSubreddit()));
    }

    protected void showDomain(@NonNull Link link) {
        mLinkDomain.setText(
                String.format(mContext.getString(R.string.link_domain), link.getDomain()));
    }

    protected void showCommentCount(@NonNull Link link) {
        int n = link.getNumComments();
        mLinkComments.setText(
                mContext.getResources().getQuantityString(R.plurals.link_comment_count, n, n));
    }

    protected String getPreviewUrl(@NonNull Link link) {
        List<Link.Preview.Image> images = link.getPreviewImages();
        if (images == null || images.size() == 0) return null;
        Link.Preview.Image imageToDisplay;
        // Retrieve preview image to display
        Link.Preview.Image image = images.get(0);
        Link.Preview.Image.Variants variants = image.getVariants();
        if (variants != null && variants.nsfw != null) {
            imageToDisplay = variants.nsfw;
        } else {
            imageToDisplay = image;
        }
        List<Link.Preview.Image.Res> resolutions = imageToDisplay.getResolutions();
        Link.Preview.Image.Res res =
                resolutions.size() > 0 ? resolutions.get(0) : imageToDisplay.getSource();
        return res.getUrl();
    }

    abstract protected void showThumbnail(
            @NonNull Link link, @NonNull ThumbnailMode mode);

    protected void loadThumbnail(@Nullable String url) {
        Picasso.with(mContext)
                .load(url)
                .fit()
                .centerCrop()
                .into(mLinkThumbnail);
    }

    protected void showNsfwTag(boolean b) {
        mNsfwIndicator.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    protected abstract void showLiked(@NonNull Link link);

    protected void showGilded(@NonNull Link link) {
        Integer gilded = link.getGilded();
        if (gilded != null && gilded > 0) {
            mGildedText.setText(String.format(mContext.getString(R.string.link_gilded_text), gilded));
            mGildedText.setVisibility(View.VISIBLE);
        } else {
            mGildedText.setVisibility(View.GONE);
        }
    }

    protected void showSaved(@NonNull Link link) {
        boolean saved = link.isSaved();
        mSavedView.setVisibility(saved ? View.VISIBLE : View.INVISIBLE);
    }

    protected void showStickied(@NonNull Link link) {
        boolean stickied = link.getStickied();
        mStickiedView.setVisibility(stickied ? View.VISIBLE : View.INVISIBLE);
    }

    protected abstract void showParentLink(boolean link);

    @Override
    public void onCreateContextMenu(
            ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        mLinkView.showLinkContextMenu(menu, view, mLink);
    }
}
