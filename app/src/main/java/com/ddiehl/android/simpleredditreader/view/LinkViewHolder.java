package com.ddiehl.android.simpleredditreader.view;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.model.listings.RedditLink;
import com.ddiehl.android.simpleredditreader.presenter.CommentsPresenter;
import com.ddiehl.android.simpleredditreader.utils.BaseUtils;
import com.squareup.picasso.Picasso;

class LinkViewHolder extends RecyclerView.ViewHolder {
    private View mLinkView;
    private TextView mLinkTitle, mLinkDomain, mLinkScore, mLinkAuthor, mLinkTimestamp,
            mLinkSubreddit, mLinkComments, mSelfText;
    private ImageView mLinkThumbnail;

    private Context mContext;
    private CommentsPresenter mCommentsPresenter;

    public LinkViewHolder(View v, CommentsPresenter presenter) {
        super(v);
        mLinkView = v.findViewById(R.id.link_view);
        mLinkTitle = (TextView) v.findViewById(R.id.link_title);
        mLinkDomain = (TextView) v.findViewById(R.id.link_domain);
        mLinkScore = (TextView) v.findViewById(R.id.link_score);
        mLinkAuthor = (TextView) v.findViewById(R.id.link_author);
        mLinkTimestamp = (TextView) v.findViewById(R.id.link_timestamp);
        mLinkSubreddit = (TextView) v.findViewById(R.id.link_subreddit);
        mLinkComments = (TextView) v.findViewById(R.id.link_comment_count);
        mLinkThumbnail = (ImageView) v.findViewById(R.id.link_thumbnail);
        mSelfText = (TextView) v.findViewById(R.id.link_self_text);

        mContext = v.getContext().getApplicationContext();
        mCommentsPresenter = presenter;
    }

    public void bind(RedditLink link) {
        if (link == null) {
            mLinkView.setVisibility(View.GONE);
            mSelfText.setVisibility(View.GONE);
            return;
        }

        mLinkView.setVisibility(View.VISIBLE);
        if (link.getSelftext() != null && !link.getSelftext().equals("")) {
            mSelfText.setText(link.getSelftext());
            mSelfText.setVisibility(View.VISIBLE);
        } else {
            mSelfText.setVisibility(View.GONE);
        }

        String createDateFormatted = BaseUtils.getFormattedDateStringFromUtc(link.getCreatedUtc().longValue());

        // Set content for each TextView
        mLinkScore.setText(String.valueOf(link.getScore()) + " points");
        mLinkTitle.setText(link.getTitle());
        mLinkAuthor.setText("/u/" + link.getAuthor());
        mLinkTimestamp.setText(createDateFormatted);
        mLinkSubreddit.setText("/r/" + link.getSubreddit());
        mLinkDomain.setText("(" + link.getDomain() + ")");
        mLinkComments.setText(link.getNumComments() + " comments");

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
    }
}
