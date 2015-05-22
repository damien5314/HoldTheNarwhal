package com.ddiehl.android.simpleredditreader.view;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.presenter.CommentsPresenter;
import com.ddiehl.android.simpleredditreader.utils.BaseUtils;
import com.ddiehl.reddit.listings.RedditComment;

class CommentViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener, View.OnCreateContextMenuListener {
    private static final String TAG = CommentViewHolder.class.getSimpleName();

    private RedditComment mRedditComment;

    private View mView;
    private View mCommentDataRow;
    private ImageView mExpanderIcon;
    private TextView mAuthorView;
    private View mSecondaryData;
    private TextView mScoreView;
    private TextView mTimestampView;
    private TextView mMoreCommentsView;
    private TextView mBodyView;

    private Context mContext;
    private CommentsPresenter mCommentsPresenter;

    public CommentViewHolder(View view, CommentsPresenter presenter) {
        super(view);
        mView = view;
        mCommentDataRow = view.findViewById(R.id.comment_data_row);
        mExpanderIcon = (ImageView) view.findViewById(R.id.comment_expander_icon);
        mAuthorView = (TextView) view.findViewById(R.id.comment_author);
        mSecondaryData = view.findViewById(R.id.comment_secondary_data);
        mScoreView = (TextView) view.findViewById(R.id.comment_score);
        mTimestampView = (TextView) view.findViewById(R.id.comment_timestamp);
        mBodyView = (TextView) view.findViewById(R.id.comment_body);

        itemView.setOnClickListener(this);
        mCommentDataRow.setOnClickListener(this);
        mBodyView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);

        mContext = view.getContext().getApplicationContext();
        mCommentsPresenter = presenter;
    }

    public void bind(final RedditComment comment) {
        mRedditComment = comment;

        // Add padding views to indentation_wrapper based on depth of comment
        int viewMargin = (comment.getDepth() - 2)
                * (int) mContext.getResources().getDimension(R.dimen.comment_indentation_margin);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) mView.getLayoutParams();
        params.setMargins(viewMargin, 0, 0, 0);

        mAuthorView.setVisibility(View.VISIBLE);
        // Set background to blue if author is OP
//        if (comment.getAuthor().equals(mRedditLink.getAuthor())) {
//            mAuthorView.setBackgroundResource(R.drawable.original_poster_background);
//            mAuthorView.setTextColor(mContext.getResources().getColor(R.color.op_text));
//        } else { // Else, set it to transparent
//            mAuthorView.setBackgroundDrawable(null);
//            mAuthorView.setTextColor(mContext.getResources().getColor(R.color.secondary_text));
//        }
        mSecondaryData.setVisibility(View.VISIBLE);
        mBodyView.setVisibility(View.VISIBLE);
        mAuthorView.setText(comment.getAuthor());
        mScoreView.setText(String.format(mContext.getString(R.string.comment_score), comment.getScore()));
        mTimestampView.setText(BaseUtils.getFormattedDateStringFromUtc(comment.getCreateUtc().longValue(), mContext));
        mBodyView.setText(comment.getBody());
        if (comment.isCollapsed()) {
            mBodyView.setVisibility(View.GONE);
            mExpanderIcon.setImageResource(R.drawable.ic_thread_expand);
        } else {
            mBodyView.setVisibility(View.VISIBLE);
            mExpanderIcon.setImageResource(R.drawable.ic_thread_collapse);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.comment_data_row:
                mCommentsPresenter.toggleThreadVisible(mRedditComment);
                break;
            default:
                v.showContextMenu();
                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        mCommentsPresenter.createContextMenu(menu, v, menuInfo, mRedditComment);
    }
}
