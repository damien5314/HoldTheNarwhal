package com.ddiehl.android.htn.listings.links;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.listings.BaseListingsPresenter;
import com.ddiehl.android.htn.listings.subreddit.ThumbnailMode;
import com.ddiehl.android.htn.utils.ThemeUtilsKt;
import com.ddiehl.android.htn.view.ColorSwapTextView;
import com.ddiehl.android.htn.view.glide.GlideApp;
import com.ddiehl.android.htn.view.markdown.HtmlParser;
import com.ddiehl.timesincetextview.TimeSinceTextView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import rxreddit.model.Image;
import rxreddit.model.Link;

public abstract class BaseLinkViewHolder extends RecyclerView.ViewHolder
        implements View.OnCreateContextMenuListener {

    protected final Context context;
    protected final BaseListingsPresenter linkPresenter;
    protected final HtmlParser htmlParser;
    protected Link link;

    protected final View view;
    protected final View savedView;
    protected final TextView linkTitle;
    protected final TextView linkDomain;
    protected final TextView linkScore;
    protected final ColorSwapTextView linkAuthor;
    protected final TextView linkSubreddit;
    protected final TextView linkComments;
    protected final TextView selfText;
    protected final TextView nsfwIndicator;
    protected final ImageView linkThumbnail;
    protected final TimeSinceTextView linkTimestamp;
    protected final TextView gildedText;
    protected final View stickiedView;

    public BaseLinkViewHolder(
            View view,
            BaseListingsPresenter presenter
    ) {
        super(view);
        this.context = view.getContext();
        this.linkPresenter = presenter;
        this.htmlParser = new HtmlParser(context);
        this.view = view.findViewById(R.id.link_view);
        savedView = view.findViewById(R.id.link_saved_view);
        linkTitle = view.findViewById(R.id.link_title);
        linkDomain = view.findViewById(R.id.link_domain);
        linkScore = view.findViewById(R.id.link_score);
        linkAuthor = view.findViewById(R.id.link_author);
        linkSubreddit = view.findViewById(R.id.link_subreddit);
        linkComments = view.findViewById(R.id.link_comment_count);
        selfText = view.findViewById(R.id.link_self_text);
        nsfwIndicator = view.findViewById(R.id.link_nsfw_indicator);
        linkThumbnail = view.findViewById(R.id.link_thumbnail);
        linkTimestamp = view.findViewById(R.id.link_timestamp);
        gildedText = view.findViewById(R.id.link_gilded_text_view);
        stickiedView = view.findViewById(R.id.link_stickied_view);
        this.view.setOnClickListener(view1 -> openLink());
        this.linkThumbnail.setOnClickListener(view1 -> openLink());
        view.findViewById(R.id.link_metadata).setOnClickListener(this::showContextMenu);
        this.itemView.setOnCreateContextMenuListener(this);
    }

    private void openLink() {
        linkPresenter.openLink(link);
    }

    private void showContextMenu(View v) {
        v.showContextMenu();
    }

    public void bind(
            @NotNull Link link, boolean showSelfText, ThumbnailMode mode,
            boolean showNsfw, boolean showParentLink) {
        this.link = link;
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
            setTextToView(selfText, link);
            selfText.setVisibility(View.VISIBLE);
        } else {
            selfText.setVisibility(View.GONE);
        }
    }

    void setTextToView(@NotNull TextView view, @NotNull Link link) {
        view.setMovementMethod(LinkMovementMethod.getInstance());
        CharSequence formatted = htmlParser.convert(link.getSelftextHtml());
        view.setText(formatted);
    }

    protected void showScore(@NotNull Link link) {
        Integer score = link.getScore();
        if (score == null) {
            final String text = context.getString(R.string.hidden_score_placeholder);
            linkScore.setText(text);
        } else {
            final String text = context.getResources()
                    .getQuantityString(R.plurals.link_score, score, score);
            linkScore.setText(text);
        }
    }

    protected void showTitle(@NotNull Link link) {
        linkTitle.setText(link.getTitle());
    }

    protected void showAuthor(@NotNull Link link) {
        linkAuthor.setText(
                String.format(context.getString(R.string.link_author), link.getAuthor()));
        String distinguished = link.getDistinguished();
        if (distinguished == null || distinguished.equals("")) {
            // noinspection deprecation
            linkAuthor.setBackgroundDrawable(linkAuthor.getOriginalBackground());
            linkAuthor.setTextColor(linkAuthor.getOriginalTextColor());
        } else {
            switch (distinguished) {
                case "moderator":
                    linkAuthor.setBackgroundResource(R.drawable.author_moderator_bg);
                    final int moderatorTextColor = ThemeUtilsKt.getColorFromAttr(context, R.attr.authorDecoratedTextColor);
                    linkAuthor.setTextColor(moderatorTextColor);
                    break;
                case "admin":
                    linkAuthor.setBackgroundResource(R.drawable.author_admin_bg);
                    final int adminTextColor = ThemeUtilsKt.getColorFromAttr(context, R.attr.authorDecoratedTextColor);
                    linkAuthor.setTextColor(adminTextColor);
                    break;
                default:
            }
        }
    }

    protected void showTimestamp(@NotNull Link link) {
        long timestamp = link.getCreatedUtc().longValue();
        linkTimestamp.setDate(timestamp);
        setEdited(link.getEdited());
    }

    private void setEdited(boolean edited) {
        CharSequence text = linkTimestamp.getText();
        linkTimestamp.setText(edited ? text + "*" : text.toString().replace("*", ""));
    }

    protected void showSubreddit(@NotNull Link link) {
        linkSubreddit.setText(
                String.format(context.getString(R.string.link_subreddit), link.getSubreddit()));
    }

    protected void showDomain(@NotNull Link link) {
        linkDomain.setText(
                String.format(context.getString(R.string.link_domain), link.getDomain()));
    }

    protected void showCommentCount(@NotNull Link link) {
        int n = link.getNumComments();
        linkComments.setText(
                context.getResources().getQuantityString(R.plurals.link_comment_count, n, n));
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
        GlideApp.with(context)
                .load(url)
                .centerCrop()
                .into(linkThumbnail);
    }

    protected void showNsfwTag(boolean b) {
        nsfwIndicator.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    protected abstract void showLiked(@NotNull Link link);

    protected void showGilded(@NotNull Link link) {
        Integer gilded = link.getGilded();
        if (gilded != null && gilded > 0) {
            gildedText.setText(String.format(context.getString(R.string.link_gilded_text), gilded));
            gildedText.setVisibility(View.VISIBLE);
        } else {
            gildedText.setVisibility(View.GONE);
        }
    }

    protected void showSaved(@NotNull Link link) {
        boolean saved = link.isSaved();
        savedView.setVisibility(saved ? View.VISIBLE : View.INVISIBLE);
    }

    protected void showStickied(@NotNull Link link) {
        boolean stickied = link.getStickied();
        stickiedView.setVisibility(stickied ? View.VISIBLE : View.INVISIBLE);
    }

    protected abstract void showParentLink(boolean link);

    @Override
    public void onCreateContextMenu(
            ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        LinkViewHolderContextMenuUtil.showLinkContextMenu(menu, view, link);
        linkPresenter.onContextMenuShownForLink(link);
    }
}
