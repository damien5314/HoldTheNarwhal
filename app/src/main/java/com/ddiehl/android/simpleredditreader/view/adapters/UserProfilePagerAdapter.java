package com.ddiehl.android.simpleredditreader.view.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.ddiehl.android.simpleredditreader.view.fragments.UserProfileOverviewFragment;

public class UserProfilePagerAdapter extends FragmentPagerAdapter {

    final int PAGE_COUNT = 3;
    private String[] mTabTitles = new String[] { "Overview, Comments", "Upvoted" };
    private Context mContext;

    public UserProfilePagerAdapter(FragmentManager fm, Context c) {
        super(fm);
        mContext = c;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return UserProfileOverviewFragment.newInstance("simpleredditreader");
            case 1:
                return UserProfileOverviewFragment.newInstance("damien5314");
            case 2:
                return UserProfileOverviewFragment.newInstance("dadmachine");
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabTitles[position];
    }
}
