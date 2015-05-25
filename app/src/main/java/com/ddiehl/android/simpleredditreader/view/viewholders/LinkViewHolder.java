package com.ddiehl.android.simpleredditreader.view.viewholders;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.reddit.listings.RedditLink;
import com.ddiehl.android.simpleredditreader.presenter.LinksPresenter;
import com.ddiehl.android.simpleredditreader.utils.BaseUtils;
import com.squareup.picasso.Picasso;

public class LinkViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener, View.OnCreateContextMenuListener {
    private static final String TAG = LinkViewHolder.class.getSimpleName();

    private RedditLink mRedditLink;

    private View mLinkView, mGildedView, mSavedText;
    private TextView mLinkTitle, mLinkDomain, mLinkScore, mLinkAuthor, mLinkTimestamp,
            mLinkSubreddit, mLinkComments, mSelfText, mGildedText;
    private ImageView mLinkThumbnail;

    private Context mContext;
    private LinksPresenter mLinksPresenter;

    public LinkViewHolder(View v, LinksPresenter presenter) {
        super(v);
        mLinkView = v.findViewById(R.id.link_view);
        mGildedView = v.findViewById(R.id.gilded_view);
        mLinkTitle = (TextView) v.findViewById(R.id.link_title);
        mLinkDomain = (TextView) v.findViewById(R.id.link_domain);
        mLinkScore = (TextView) v.findViewById(R.id.link_score);
        mLinkAuthor = (TextView) v.findViewById(R.id.link_author);
        mLinkTimestamp = (TextView) v.findViewById(R.id.link_timestamp);
        mLinkSubreddit = (TextView) v.findViewById(R.id.link_subreddit);
        mLinkComments = (TextView) v.findViewById(R.id.link_comment_count);
        mLinkThumbnail = (ImageView) v.findViewById(R.id.link_thumbnail);
        mSelfText = (TextView) v.findViewById(R.id.link_self_text);
        mGildedText = (TextView) v.findViewById(R.id.link_gilded_text_view);
        mSavedText = v.findViewById(R.id.link_saved_view);

        itemView.setOnClickListener(this);
        mLinkTitle.setOnClickListener(this);
        mLinkThumbnail.setOnClickListener(this);
        mLinkComments.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);

        mContext = v.getContext().getApplicationContext();
        mLinksPresenter = presenter;
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

        String createDateFormatted = BaseUtils.getFormattedDateStringFromUtc(link.getCreatedUtc().longValue(), mContext);

        // Set content for each TextView
        mLinkScore.setText(String.valueOf(String.format("%s points", link.getScore())));
        mLinkTitle.setText(link.getTitle());
        mLinkAuthor.setText(String.format("/u/%s", link.getAuthor()));
        mLinkTimestamp.setText(createDateFormatted);
        mLinkSubreddit.setText(String.format("/r/%s", link.getSubreddit()));
        mLinkDomain.setText(String.format("(%s)", link.getDomain()));
        mLinkComments.setText(String.format("%s comments", link.getNumComments()));
        mSavedText.setVisibility(link.isSaved() ? View.VISIBLE : View.GONE);

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
            mLinkView.setBackgroundResource(R.drawable.link_card_background);
        } else if (link.isLiked()) {
            mLinkView.setBackgroundResource(R.drawable.link_card_background_upvoted);
        } else {
            mLinkView.setBackgroundResource(R.drawable.link_card_background_downvoted);
        }

        // Show gilding view if appropriate, else hide
        Integer gilded = link.getGilded();
        if (gilded != null && gilded > 0) {
            mGildedText.setText(String.format(mContext.getString(R.string.gilded_text_view), gilded));
            mGildedView.setVisibility(View.VISIBLE);
        } else {
            mGildedView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.link_title:
            case R.id.link_thumbnail:
                mLinksPresenter.openLink(mRedditLink);
                break;
            case R.id.link_comment_count:
                mLinksPresenter.openCommentsForLink(mRedditLink);
                break;
            default:
                v.showContextMenu();
                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        mLinksPresenter.showContextMenu(menu, v, menuInfo, mRedditLink);
    }
}
