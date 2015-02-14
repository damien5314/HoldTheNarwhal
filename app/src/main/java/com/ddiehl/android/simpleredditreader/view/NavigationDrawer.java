package com.ddiehl.android.simpleredditreader.view;

import com.ddiehl.android.simpleredditreader.R;

import java.util.ArrayList;
import java.util.List;

public class NavigationDrawer {
    private static final String TAG = NavigationDrawer.class.getSimpleName();

    private static NavigationDrawer _instance = new NavigationDrawer();

    private List<DrawerItem> mDrawerItems;

    private NavigationDrawer() {
        mDrawerItems = new ArrayList<>();
        mDrawerItems.add(new DrawerItem(R.id.drawer_log_in, 0, R.string.drawer_log_in));
        mDrawerItems.add(new DrawerItem(R.id.drawer_user_profile, 0, R.string.drawer_user_profile));
        mDrawerItems.add(new DrawerItem(R.id.drawer_front_page, 0, R.string.drawer_front_page));
        mDrawerItems.add(new DrawerItem(R.id.drawer_r_all, 0, R.string.drawer_r_all));
        mDrawerItems.add(new DrawerItem(R.id.drawer_subreddits, 0, R.string.drawer_subreddits));
        mDrawerItems.add(new DrawerItem(R.id.drawer_random_subreddit, 0, R.string.drawer_random_subreddit));
    }

    public static NavigationDrawer getInstance() {
        return _instance;
    }

    public List<DrawerItem> getItems() {
        return mDrawerItems;
    }

    public DrawerItem get(int position) {
        return mDrawerItems.get(position);
    }

    public static class DrawerItem {
        private int mId;
        private int mIconResourceId;
        private int mLabelResourceId;

        public DrawerItem(int id, int iconResourceId, int labelResourceId) {
            mId = id;
            mIconResourceId = iconResourceId;
            mLabelResourceId = labelResourceId;
        }

        public int getId() {
            return mId;
        }

        public int getIconResourceId() {
            return mIconResourceId;
        }

        public int getItemLabel() {
            return mLabelResourceId;
        }
    }
}
