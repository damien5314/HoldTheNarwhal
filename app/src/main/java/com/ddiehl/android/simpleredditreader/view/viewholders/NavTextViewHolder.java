package com.ddiehl.android.simpleredditreader.view.viewholders;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.presenter.MainPresenter;
import com.ddiehl.reddit.identity.UserIdentity;

public class NavTextViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

    private Context mContext;
    private View mItemRow;
    private ImageView mItemIcon;
    private TextView mItemLabel;

    private MainPresenter mMainPresenter;

    private static final NavItem[] sItems = new NavItem[] {
            new NavItem(R.id.drawer_log_in, R.string.drawer_log_in),
            new NavItem(R.id.drawer_user_profile, R.string.drawer_user_profile),
            new NavItem(R.id.drawer_subreddits, R.string.drawer_subreddits),
            new NavItem(R.id.drawer_front_page, R.string.drawer_front_page),
            new NavItem(R.id.drawer_r_all, R.string.drawer_r_all),
            new NavItem(R.id.drawer_random_subreddit, R.string.drawer_random_subreddit),
    };

    public NavTextViewHolder(View itemView, MainPresenter presenter) {
        super(itemView);
        mContext = itemView.getContext();
        mMainPresenter = presenter;
        mItemRow = itemView.findViewById(R.id.navigation_drawer_item);
        mItemIcon = (ImageView) itemView.findViewById(R.id.navigation_drawer_item_icon);
        mItemLabel = (TextView) itemView.findViewById(R.id.navigation_drawer_item_text);
    }

    public void bind(int position) {

        UserIdentity user = mMainPresenter.getAuthenticatedUser();

        if (user == null) {
            switch (position) {
                case 0: bind2(0); break;
                case 1: bind2(3); break;
                case 2: bind2(4); break;
                case 3: bind2(5); break;
            }
        } else {
            switch (position) {
                case 0: bind2(1); break;
                case 1: bind2(2); break;
                case 2: bind2(3); break;
                case 3: bind2(4); break;
                case 4: bind2(5); break;
            }
        }
    }

    private void bind2(int position) {
        mItemRow.setId(sItems[position].id);
        mItemLabel.setText(mContext.getString(sItems[position].labelId));
        mItemRow.setOnClickListener(this);
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
                mMainPresenter.showUserSubreddits();
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

    private static class NavItem {
        int id;
        int labelId;

        NavItem(int id, int labelId) {
            this.id = id;
            this.labelId = labelId;
        }
    }
}