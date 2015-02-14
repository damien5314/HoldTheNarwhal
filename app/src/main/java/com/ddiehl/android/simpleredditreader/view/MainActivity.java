package com.ddiehl.android.simpleredditreader.view;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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


public class MainActivity extends ActionBarActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    // Navigation drawer
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerItem[] mDrawerItems = new DrawerItem[] {
            new DrawerItem(0, R.string.drawer_log_in),
            new DrawerItem(0, R.string.drawer_user_profile),
            new DrawerItem(0, R.string.drawer_front_page),
            new DrawerItem(0, R.string.drawer_all),
            new DrawerItem(0, R.string.drawer_subreddits),
            new DrawerItem(0, R.string.drawer_random_subreddit)
    };
    private CharSequence mLastFragmentTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerListView = (ListView) findViewById(R.id.drawer_list);
        mDrawerListView.setAdapter(new NavigationDrawerItemAdapter(this, mDrawerItems));
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
                R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
//                setTitle(R.string.app_name);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
//                setTitle(mLastFragmentTitle);
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_drawer);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            Fragment defaultFragment = ListingFragment.newInstance(null);
            displayFragment(defaultFragment);
        }
    }

    private void selectItem(int position) {
        Fragment fragment;
        switch (position) {
            case 0: // Log In
                break;
            case 1: // User Profile
                break;
            case 2: // Front Page
                fragment = ListingFragment.newInstance(null);
                displayFragment(fragment);
                break;
            case 3: // /r/all
                fragment = ListingFragment.newInstance("all");
                displayFragment(fragment);
                break;
            case 4: // Subreddits
                break;
            case 5: // Random Subreddit
                fragment = ListingFragment.newInstance("random");
                displayFragment(fragment);
                break;
        }

        mDrawerListView.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerListView);
    }

    private void displayFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        Fragment currentFragment = fm.findFragmentById(R.id.fragment_container);
        if (currentFragment != null) {
            transaction.remove(currentFragment);
        }

        if (fragment != null) {
            transaction.add(R.id.fragment_container, fragment);
        }

        transaction.commit();
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

    private class NavigationDrawerItemAdapter extends ArrayAdapter<DrawerItem> {
        public NavigationDrawerItemAdapter(Context context, DrawerItem[] data) {
            super(context, 0, data);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.navigation_drawer_item, null);
            }

            ((TextView) view.findViewById(R.id.drawer_item_label))
                    .setText(getString(mDrawerItems[position].getItemLabel()));

            return view;
        }
    }
}
