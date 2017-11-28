package com.ddiehl.android.htn.listings.links;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.listings.BaseListingsPresenter;
import com.ddiehl.android.htn.listings.subreddit.ThumbnailMode;
import com.ddiehl.android.htn.view.ColorSwapTextView;
import com.ddiehl.android.htn.view.glide.GlideApp;
import com.ddiehl.android.htn.view.markdown.HtmlParser;
import com.ddiehl.android.htn.view.markdown.MarkdownParser;
import com.ddiehl.timesincetextview.TimeSinceTextView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rxreddit.model.Image;
import rxreddit.model.Link;

public abstract class BaseLinkViewHolder extends RecyclerView.ViewHolder
        implements View.OnCreateContextMenuListener {

    protected final Context mContext;
    protected final LinkView mLinkView;
    protected final BaseListingsPresenter mLinkPresenter;
    protected Link mLink;

    @Inject @Nullable MarkdownParser mMarkdownParser;
    @Inject HtmlParser mHtmlParser;

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

    public BaseLinkViewHolder(View view, LinkView linkView, BaseListingsPresenter presenter) {
        super(view);
        HoldTheNarwhal.getApplicationComponent().inject(this);
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
            @NotNull Link link, boolean showSelfText, ThumbnailMode mode,
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

    protected void showSelfText(@NotNull Link link, boolean showSelfText) {
        if (link.getSelftext() != null && !"".equals(link.getSelftext()) && showSelfText) {
            setTextToView(mSelfText, link);
            mSelfText.setVisibility(View.VISIBLE);
        } else {
            mSelfText.setVisibility(View.GONE);
        }
    }

    void setTextToView(@NotNull TextView view, @NotNull Link link) {
        view.setMovementMethod(LinkMovementMethod.getInstance());
        if (mMarkdownParser != null) {
            CharSequence formatted = mMarkdownParser.convert(link.getSelftext());
            view.setText(formatted);
        } else {
            Spanned formatted = mHtmlParser.convert(link.getSelftextHtml());
            view.setText(formatted);
        }
    }

    protected void showScore(@NotNull Link link) {
        Integer score = link.getScore();
        if (score == null) {
            mLinkScore.setText(
                    mContext.getString(R.string.hidden_score_placeholder));
        } else {
            mLinkScore.setText(
                    mContext.getResources().getQuantityString(R.plurals.link_score, score, score));
        }
    }

    protected void showTitle(@NotNull Link link) {
        mLinkTitle.setText(link.getTitle());
    }

    protected void showAuthor(@NotNull Link link) {
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

    protected void showTimestamp(@NotNull Link link) {
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

    protected void showSubreddit(@NotNull Link link) {
        mLinkSubreddit.setText(
                String.format(mContext.getString(R.string.link_subreddit), link.getSubreddit()));
    }

    protected void showDomain(@NotNull Link link) {
        mLinkDomain.setText(
                String.format(mContext.getString(R.string.link_domain), link.getDomain()));
    }

    protected void showCommentCount(@NotNull Link link) {
        int n = link.getNumComments();
        mLinkComments.setText(
                mContext.getResources().getQuantityString(R.plurals.link_comment_count, n, n));
    }

    protected String getPreviewUrl(@NotNull Link link) {
        List<Image> images = link.getPreviewImages();
        if (images == null || images.size() == 0) return null;
        Image imageToDisplay;
        // Retrieve preview image to display
        Image image = images.get(0);
        Image.Variants variants = image.getVariants();
        if (variants != null && variants.getNsfw() != null) {
            imageToDisplay = variants.getNsfw();
        } else {
            imageToDisplay = image;
        }
        List<Image.Res> resolutions = imageToDisplay.getResolutions();
        Image.Res res =
                resolutions.size() > 0 ? resolutions.get(0) : imageToDisplay.getSource();
        return res.getUrl();
    }

    abstract protected void showThumbnail(
            @NotNull Link link, @NotNull ThumbnailMode mode);

    protected void loadThumbnail(@Nullable String url) {
        GlideApp.with(mContext)
                .load(url)
                .centerCrop()
                .into(mLinkThumbnail);
    }

    protected void showNsfwTag(boolean b) {
        mNsfwIndicator.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    protected abstract void showLiked(@NotNull Link link);

    protected void showGilded(@NotNull Link link) {
        Integer gilded = link.getGilded();
        if (gilded != null && gilded > 0) {
            mGildedText.setText(String.format(mContext.getString(R.string.link_gilded_text), gilded));
            mGildedText.setVisibility(View.VISIBLE);
        } else {
            mGildedText.setVisibility(View.GONE);
        }
    }

    protected void showSaved(@NotNull Link link) {
        boolean saved = link.isSaved();
        mSavedView.setVisibility(saved ? View.VISIBLE : View.INVISIBLE);
    }

    protected void showStickied(@NotNull Link link) {
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
