package com.ddiehl.android.simpleredditreader.view;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.presenter.MainPresenter;

public class NavTextViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

    private Context mContext;
    private View mItemRow;
    private ImageView mItemIcon;
    private TextView mItemLabel;

    private MainPresenter mMainPresenter;

    public NavTextViewHolder(View itemView, MainPresenter presenter) {
        super(itemView);
        mContext = itemView.getContext();
        mMainPresenter = presenter;
        mItemRow = itemView.findViewById(R.id.navigation_drawer_item);
        mItemIcon = (ImageView) itemView.findViewById(R.id.navigation_drawer_item_icon);
        mItemLabel = (TextView) itemView.findViewById(R.id.navigation_drawer_item_text);
    }

    public void bind(int position) {
        switch (position) {
            // Set label, icon, and onClick behavior for the row
            case 0:
                mItemLabel.setText(mContext.getString(R.string.drawer_log_in));
                mItemRow.setId(R.id.drawer_log_in);
                mItemRow.setOnClickListener(this);
                break;
            case 1:
                mItemLabel.setText(mContext.getString(R.string.drawer_user_profile));
                mItemRow.setId(R.id.drawer_user_profile);
                mItemRow.setOnClickListener(this);
                break;
            case 2:
                mItemLabel.setText(mContext.getString(R.string.drawer_subreddits));
                mItemRow.setId(R.id.drawer_subreddits);
                mItemRow.setOnClickListener(this);
                break;
            case 3:
                mItemLabel.setText(mContext.getString(R.string.drawer_front_page));
                mItemRow.setId(R.id.drawer_front_page);
                mItemRow.setOnClickListener(this);
                break;
            case 4:
                mItemLabel.setText(mContext.getString(R.string.drawer_r_all));
                mItemRow.setId(R.id.drawer_r_all);
                mItemRow.setOnClickListener(this);
                break;
            case 5:
                mItemLabel.setText(mContext.getString(R.string.drawer_random_subreddit));
                mItemRow.setId(R.id.drawer_random_subreddit);
                mItemRow.setOnClickListener(this);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.drawer_log_in:
                mMainPresenter.presentLoginView();
                break;
            case R.id.drawer_user_profile:
                mMainPresenter.showUserProfile(null);
                break;
            case R.id.drawer_subreddits:
                mMainPresenter.showSubreddits();
                break;
            case R.id.drawer_front_page:
                mMainPresenter.showSubreddit(null);
                break;
            case R.id.drawer_r_all:
                mMainPresenter.showSubreddit("all");
                break;
            case R.id.drawer_random_subreddit:
                mMainPresenter.showSubreddit("random");
                break;
        }
    }
}