package com.ddiehl.android.simpleredditreader.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;


public class MainActivity extends ActionBarActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

//    public static final String EXTRA_SUBREDDIT = "com.ddiehl.android.simpleredditreader.extra_subreddit";

    // Navigation drawer
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private String[] mDrawerItems = new String[] {
            "Log In",
            "User Profile",
            "Front Page",
            "/r/all",
            "Subreddits",
            "Random Subreddit"
    };

//    private String mSubreddit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Bundle extras = getIntent().getExtras();
//        if (extras != null
//                && extras.containsKey(EXTRA_SUBREDDIT)) {
//            mSubreddit = extras.getString(EXTRA_SUBREDDIT);
//        }
//
//        // If started as the launcher activity, default to /r/all
//        if (mSubreddit == null) {
//            mSubreddit = getString(R.string.default_subreddit);
//        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerListView = (ListView) findViewById(R.id.drawer_list);
        mDrawerListView.setAdapter(new NavigationDrawerItemAdapter(this, mDrawerItems));
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        Fragment defaultFragment = ListingFragment.newInstance(getString(R.string.default_subreddit));
        displayFragment(defaultFragment);
    }

    private void selectItem(int position) {
        Log.d(TAG, "Navigation drawer item selected: " + mDrawerItems[position]);
    }

    private void displayFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    private class NavigationDrawerItemAdapter extends ArrayAdapter<String> {
        public NavigationDrawerItemAdapter(Context context, String[] data) {
            super(context, 0, data);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.navigation_drawer_item, null);
            }

            ((TextView) view.findViewById(R.id.drawer_item_label)).setText(mDrawerItems[position]);

            return view;
        }
    }
}
