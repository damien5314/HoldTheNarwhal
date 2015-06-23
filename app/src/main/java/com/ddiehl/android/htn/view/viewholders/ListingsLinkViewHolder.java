package com.ddiehl.android.htn.view.viewholders;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.LinkPresenter;
import com.ddiehl.android.htn.view.widgets.RedditDateTextView;
import com.ddiehl.reddit.listings.RedditLink;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ListingsLinkViewHolder extends RecyclerView.ViewHolder
        implements View.OnCreateContextMenuListener {
    private static final String TAG = ListingsLinkViewHolder.class.getSimpleName();

    private Context mContext;
    private LinkPresenter mLinkPresenter;
    private RedditLink mRedditLink;

    @InjectView(R.id.link_view) View mLinkView;
    @InjectView(R.id.link_saved_view) View mSavedView;
    @InjectView(R.id.link_title) TextView mLinkTitle;
    @InjectView(R.id.link_domain) TextView mLinkDomain;
    @InjectView(R.id.link_score) TextView mLinkScore;
    @InjectView(R.id.link_author) TextView mLinkAuthor;
    @InjectView(R.id.link_subreddit) TextView mLinkSubreddit;
    @InjectView(R.id.link_comment_count) TextView mLinkComments;
    @InjectView(R.id.link_self_text) TextView mSelfText;
    @InjectView(R.id.link_thumbnail) ImageView mLinkThumbnail;
    @InjectView(R.id.link_timestamp) RedditDateTextView mLinkTimestamp;
    @InjectView(R.id.gilded_view) View mGildedView;
    @InjectView(R.id.link_gilded_text_view) TextView mGildedText;
    @InjectView(R.id.link_stickied_view) View mStickiedView;

    public ListingsLinkViewHolder(View v, LinkPresenter presenter) {
        super(v);
        mContext = v.getContext();
        mLinkPresenter = presenter;
        ButterKnife.inject(this, v);
        itemView.setOnCreateContextMenuListener(this);
    }

    @OnClick({ R.id.link_title, R.id.link_thumbnail })
    void openLink() {
        mLinkPresenter.openLink(mRedditLink);
    }

    @OnClick(R.id.link_comment_count)
    void showCommentsForLink() {
        mLinkPresenter.showCommentsForLink(mRedditLink);
    }

    @OnClick(R.id.link_view)
    void showContextMenu(View v) {
        v.showContextMenu();
    }

    public void bind(RedditLink link, boolean showSelfText) {
        mRedditLink = link;

        if (link == null) {
            mLinkView.setVisibility(View.GONE);
            mSelfText.setVisibility(View.GONE);
            return;
        }

        mLinkView.setVisibility(View.VISIBLE);
        if (link.getSelftext() != null && !link.getSelftext().equals("") && showSelfText) {
            mSelfText.setText(link.getSelftext());
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

        String thumbnailUrl = link.getThumbnail();
        switch (thumbnailUrl) {
            case "nsfw":
                Picasso.with(mContext)
                        .load(R.drawable.ic_nsfw)
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
            mGildedView.setVisibility(View.VISIBLE);
        } else {
            mGildedView.setVisibility(View.GONE);
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
        mLinkPresenter.showLinkContextMenu(menu, v, menuInfo, mRedditLink);
    }
}
