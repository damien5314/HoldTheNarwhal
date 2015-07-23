/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view.viewholders;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.LinkPresenter;
import com.ddiehl.android.htn.view.widgets.RedditDateTextView;
import com.ddiehl.reddit.listings.Link;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.uncod.android.bypass.Bypass;

public class ListingsLinkViewHolder extends RecyclerView.ViewHolder
        implements View.OnCreateContextMenuListener {

    private Context mContext;
    private Bypass mBypass;
    private LinkPresenter mLinkPresenter;
    private Link mLink;

    @Bind(R.id.link_view) View mLinkView;
    @Bind(R.id.link_saved_view) View mSavedView;
    @Bind(R.id.link_title) TextView mLinkTitle;
    @Bind(R.id.link_domain) TextView mLinkDomain;
    @Bind(R.id.link_score) TextView mLinkScore;
    @Bind(R.id.link_author) TextView mLinkAuthor;
    @Bind(R.id.link_subreddit) TextView mLinkSubreddit;
    @Bind(R.id.link_comment_count) TextView mLinkComments;
    @Bind(R.id.link_self_text) TextView mSelfText;
    @Bind(R.id.link_thumbnail) ImageView mLinkThumbnail;
    @Bind(R.id.link_timestamp) RedditDateTextView mLinkTimestamp;
    @Bind(R.id.link_gilded_text_view) TextView mGildedText;
    @Bind(R.id.link_stickied_view) View mStickiedView;

    public ListingsLinkViewHolder(View v, LinkPresenter presenter) {
        super(v);
        mContext = v.getContext().getApplicationContext();
        mBypass = new Bypass(mContext);
        mLinkPresenter = presenter;
        ButterKnife.bind(this, v);
        itemView.setOnCreateContextMenuListener(this);
    }

    @OnClick({ R.id.link_title, R.id.link_thumbnail })
    void openLink() {
        mLinkPresenter.openLink(mLink);
    }

    @OnClick(R.id.link_comment_count)
    void showCommentsForLink() {
        mLinkPresenter.showCommentsForLink(mLink);
    }

    @OnClick(R.id.link_view)
    void showContextMenu(View v) {
        v.showContextMenu();
    }

    public void bind(Link link, boolean showSelfText) {
        mLink = link;

        if (link == null) {
            mLinkView.setVisibility(View.GONE);
            mSelfText.setVisibility(View.GONE);
            return;
        }

        mLinkView.setVisibility(View.VISIBLE);
        if (link.getSelftext() != null && !link.getSelftext().equals("") && showSelfText) {
            // Bypass markdown formatting
            CharSequence formatted = mBypass.markdownToSpannable(link.getSelftext());
            mSelfText.setText(formatted);
            mSelfText.setMovementMethod(LinkMovementMethod.getInstance());
            mSelfText.setVisibility(View.VISIBLE);
        } else {
            mSelfText.setVisibility(View.GONE);
        }

        // Set content for each TextView
        mLinkScore.setText(String.format(mContext.getString(R.string.link_score), link.getScore()));
        mLinkTitle.setText(link.getTitle());
        mLinkAuthor.setText(String.format(mContext.getString(R.string.link_author), link.getAuthor()));
        String distinguished = link.getDistinguished();
        if (distinguished == null || distinguished.equals("")) {
            mLinkAuthor.setBackgroundResource(0);
            mLinkAuthor.setTextColor(mContext.getResources().getColor(android.R.color.white));
        } else {
            switch (distinguished) {
                case "moderator":
                    mLinkAuthor.setBackgroundResource(R.drawable.author_moderator_bg);
                    mLinkAuthor.setTextColor(mContext.getResources().getColor(R.color.author_moderator_text));
                    break;
                case "admin":
                    mLinkAuthor.setBackgroundResource(R.drawable.author_admin_bg);
                    mLinkAuthor.setTextColor(mContext.getResources().getColor(R.color.author_admin_text));
                    break;
                default:

            }
        }
        mLinkTimestamp.setDate(link.getCreatedUtc().longValue());
        if (link.isEdited() != null) {
            switch (link.isEdited()) {
                case "":
                case "0":
                case "false":
                    mLinkTimestamp.setEdited(false);
                    break;
                default:
                    mLinkTimestamp.setEdited(true);
            }
        }
        mLinkSubreddit.setText(String.format(mContext.getString(R.string.link_subreddit), link.getSubreddit()));
        mLinkDomain.setText(String.format(mContext.getString(R.string.link_domain), link.getDomain()));
        mLinkComments.setText(String.format(mContext.getString(R.string.link_comment_count), link.getNumComments()));

        List<Link.Preview.Image> images = link.getPreviewImages();
        if (images != null && images.size() > 0) {
            Link.Preview.Image imageToDisplay = null;
            // Retrieve preview image to display
            Link.Preview.Image image = images.get(0);
            Link.Preview.Image.Variants variants = image.getVariants();
            if (variants != null && variants.nsfw != null) {
                imageToDisplay = variants.nsfw;
            } else {
                imageToDisplay = image;
            }
            // Set selected image to ImageView for thumbnail
            mLinkThumbnail.setVisibility(View.VISIBLE);
            List<Link.Preview.Image.Res> resolutions = imageToDisplay.getResolutions();
            Link.Preview.Image.Res res = resolutions.size() > 0 ? resolutions.get(0) : imageToDisplay.getSource();
            Picasso.with(mContext)
                    .load(res.getUrl())
                    .placeholder(R.drawable.ic_thumbnail_placeholder)
                    .error(R.drawable.ic_alert_error)
                    .into(mLinkThumbnail);
        } else { // Default to the old logic with thumbnail field
            String thumbnailUrl = link.getThumbnail();
            switch (thumbnailUrl) {
                case "nsfw":
                    Picasso.with(mContext)
                            .load(R.drawable.ic_nsfw2)
                            .into(mLinkThumbnail);
                    break;
                case "": case "default": case "self":
                    mLinkThumbnail.setVisibility(View.GONE);
                    break;
                default:
                    Picasso.with(mContext)
                            .load(thumbnailUrl)
                            .placeholder(R.drawable.ic_thumbnail_placeholder)
                            .error(R.drawable.ic_alert_error)
                            .into(mLinkThumbnail);
                    mLinkThumbnail.setVisibility(View.VISIBLE);
            }
        }

        // Set background tint based on isLiked
        if (link.isLiked() == null) {
            mLinkView.setBackgroundResource(R.drawable.listings_card_bg);
        } else if (link.isLiked()) {
            mLinkView.setBackgroundResource(R.drawable.listings_card_upvoted_bg);
        } else {
            mLinkView.setBackgroundResource(R.drawable.listings_card_downvoted_bg);
        }

        // Show gilding view if appropriate, else hide
        Integer gilded = link.getGilded();
        if (gilded != null && gilded > 0) {
            mGildedText.setText(String.format(mContext.getString(R.string.link_gilded_text), gilded));
            mGildedText.setVisibility(View.VISIBLE);
        } else {
            mGildedText.setVisibility(View.GONE);
        }

        // Show saved view if appropriate, else hide
        Boolean saved = link.isSaved();
        mSavedView.setVisibility(saved != null && saved ? View.VISIBLE : View.INVISIBLE);

        // Show stickied view if appropriate, else hide
        Boolean stickied = link.getStickied();
        mStickiedView.setVisibility(stickied != null && stickied ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        mLinkPresenter.showLinkContextMenu(menu, v, menuInfo, mLink);
    }
}
