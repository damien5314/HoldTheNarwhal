package com.ddiehl.android.simpleredditreader.view;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;

public class NavigationDrawer {
    private static final String TAG = NavigationDrawer.class.getSimpleName();

    private static final int mDrawerLayoutId = R.id.drawer_layout;

    private String[] mDrawerItems = new String[] {
            "Log In",
            "User Profile",
            "Front Page",
            "/r/all",
            "Subreddits",
            "Random Subreddit"
    };
    private DrawerLayout mLayout;
    private ListView mListView;
    private LayoutInflater mLayoutInflater;

    public NavigationDrawer(LayoutInflater inflater, View v) {
        mLayoutInflater = inflater;
        mLayout = (DrawerLayout) v.findViewById(R.id.drawer_layout);
        mListView = (ListView) v.findViewById(R.id.drawer_list);
        mListView.setAdapter(new NavigationDrawerItemAdapter(v.getContext(), mDrawerItems));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
    }

    private void selectItem(int position) {
        Log.d(TAG, "Navigation drawer item selected: " + mDrawerItems[position]);
    }

    public DrawerLayout getLayout() {
        return mLayout;
    }

    public ListView getListView() {
        return mListView;
    }

    private class NavigationDrawerItemAdapter extends ArrayAdapter<String> {
        public NavigationDrawerItemAdapter(Context context, String[] data) {
            super(context, 0, data);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = mLayoutInflater.inflate(R.layout.navigation_drawer_item, null);
            }

            ((TextView) view.findViewById(R.id.drawer_item_label)).setText(mDrawerItems[position]);

            return view;
        }
    }
}
