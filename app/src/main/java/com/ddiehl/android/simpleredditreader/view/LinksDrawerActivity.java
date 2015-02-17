package com.ddiehl.android.simpleredditreader.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.Sort;
import com.ddiehl.android.simpleredditreader.TimeSpan;

import java.util.List;

public class LinksDrawerActivity extends ActionBarActivity {
    private static final String TAG = LinksDrawerActivity.class.getSimpleName();

    public static final String EXTRA_SUBREDDIT = "com.ddiehl.android.simpleredditreader.extra_subreddit";

    // Navigation drawer
    private NavigationDrawer mNavigationDrawer;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private ActionBarDrawerToggle mDrawerToggle;

//    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawer = NavigationDrawer.getInstance();

        mDrawerListView = (ListView) findViewById(R.id.drawer_list);
        mDrawerListView.setAdapter(new NavigationDrawerItemAdapter(this, mNavigationDrawer.getItems()));
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_drawer);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        String subreddit = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            subreddit = extras.getString(EXTRA_SUBREDDIT);
        }

        Fragment fragment = LinksFragment.newInstance(subreddit, Sort.HOT, TimeSpan.ALL);
        displayFragment(fragment);

//        mViewPager = (ViewPager) findViewById(R.id.view_pager);
//        mViewPager.setAdapter(new LinkFragmentPagerAdapter(getSupportFragmentManager()));
    }

    private void selectItem(int position) {
        LinksFragment fragment = (LinksFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        Intent i;
        switch (mNavigationDrawer.get(position).getId()) {
            case R.id.drawer_log_in:
                break;
            case R.id.drawer_user_profile:
                break;
            case R.id.drawer_front_page:
//                fragment = LinksFragment.newInstance(null);
//                displayFragment(fragment);
                fragment.updateSubreddit(null);
                break;
            case R.id.drawer_r_all:
//                fragment = LinksFragment.newInstance("all");
//                displayFragment(fragment);
                fragment.updateSubreddit("all");
                break;
            case R.id.drawer_subreddits:
                break;
            case R.id.drawer_random_subreddit:
//                fragment = LinksFragment.newInstance("random");
//                displayFragment(fragment);
                fragment.updateSubreddit("random");
                break;
        }

        mDrawerListView.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerListView);
    }

    protected void displayFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item))
            return true;

        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    private class NavigationDrawerItemAdapter extends ArrayAdapter<NavigationDrawer.DrawerItem> {
        public NavigationDrawerItemAdapter(Context context, List<NavigationDrawer.DrawerItem> data) {
            super(context, 0, data);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.navigation_drawer_item, null);
            }

            ((TextView) view.findViewById(R.id.drawer_item_label))
                    .setText(getString(mNavigationDrawer.get(position).getItemLabel()));

            return view;
        }
    }

//    private static class LinkFragmentPagerAdapter extends FragmentPagerAdapter {
//
//        public LinkFragmentPagerAdapter(FragmentManager fm) {
//            super(fm);
//        }
//
//        @Override
//        public Fragment getItem(int position) {
//
//            // 0 = Hot
//            // 1 = New
//            return null;
//        }
//
//        @Override
//        public int getCount() {
//            return 2;
//        }
//    }
}
