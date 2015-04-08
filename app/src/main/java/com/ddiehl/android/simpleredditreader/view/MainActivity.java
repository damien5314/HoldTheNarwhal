package com.ddiehl.android.simpleredditreader.view;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.web.RedditAuthProxy;
import com.squareup.otto.Bus;

public class MainActivity extends ActionBarActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String EXTRA_SUBREDDIT = "com.ddiehl.android.simpleredditreader.extra_subreddit";
    public static final int REQUEST_AUTHORIZE = 1000;

    private Bus mBus;

    // Navigation drawer
    private View mNavigationDrawer;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private RecyclerView mNavigationDrawerListView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mLayoutAdapter;

    private ProgressDialog mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBus = BusProvider.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_drawer);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mNavigationDrawerListView = (RecyclerView) findViewById(R.id.navigation_drawer_list_view);
        mNavigationDrawerListView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        mLayoutManager = new LinearLayoutManager(this);
        mNavigationDrawerListView.setLayoutManager(mLayoutManager);
        mLayoutAdapter = new NavDrawerItemAdapter();
        mNavigationDrawerListView.setAdapter(mLayoutAdapter);

//        mNavToSubredditGo = findViewById(R.id.drawer_navigate_to_subreddit_go);
//        mNavToSubredditGo.setOnClickListener(this);
//
//        mNavToSubredditText = (EditText) findViewById(R.id.drawer_navigate_to_subreddit_text);
//        mNavToSubredditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (!hasFocus) { // Hide keyboard
//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                }
//            }
//        });
//        mNavToSubredditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                    mNavToSubredditGo.performClick();
//                    return true;
//                }
//                return false;
//            }
//        });

        // Set onClick to null to intercept click events from background
        mNavigationDrawer = findViewById(R.id.navigation_drawer);
        mNavigationDrawer.setOnClickListener(null);
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
    protected void onStart() {
        super.onStart();
        mBus.register(this);

        Fragment currentFragment = getCurrentFragment();
        if (currentFragment == null) {
            showSubreddit(null);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBus.unregister(this);
    }

    public Fragment getCurrentFragment() {
        FragmentManager fm = getSupportFragmentManager();
        return fm.findFragmentById(R.id.fragment_container);
    }

    public void showSubreddit(String subreddit) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment currentFragment = fm.findFragmentById(R.id.fragment_container);
        // If the current fragment is a LinksFragment, just update the subreddit
        // Else, swap in a LinksFragment
        if (currentFragment instanceof LinksFragment) {
            ((LinksFragment) currentFragment).updateSubreddit(subreddit);
        } else {
            Fragment newFragment = LinksFragment.newInstance(subreddit);
            fm.beginTransaction()
                    .replace(R.id.fragment_container, newFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    public void openWebViewForURL(String url) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment newFragment = WebViewFragment.newInstance(url);
        fm.beginTransaction()
                .replace(R.id.fragment_container, newFragment)
                .addToBackStack(null)
                .commit();
    }

    public void showSpinner(String message) {
        if (mProgressBar == null) {
            mProgressBar = new ProgressDialog(this, R.style.ProgressDialog);
            mProgressBar.setCancelable(false);
            mProgressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        mProgressBar.setMessage(message);
        mProgressBar.show();
    }

    public void dismissSpinner() {
        if (mProgressBar != null && mProgressBar.isShowing()) {
            mProgressBar.dismiss();
        }
    }

    private class NavDrawerItemAdapter extends RecyclerView.Adapter<NavDrawerItemHolder> {
        @Override
        public NavDrawerItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.navigation_drawer_row, parent, false);
            return new NavDrawerItemHolder(v);
        }

        @Override
        public void onBindViewHolder(NavDrawerItemHolder holder, int position) {
            holder.displayPosition(position);
        }

        @Override
        public int getItemCount() {
            return 7;
        }
    }

    private class NavDrawerItemHolder extends RecyclerView.ViewHolder {
        private View mItemRow;
        private ImageView mItemIcon;
        private TextView mItemLabel;

        public NavDrawerItemHolder(View itemView) {
            super(itemView);
            mItemRow = itemView.findViewById(R.id.navigation_drawer_item);
            mItemIcon = (ImageView) itemView.findViewById(R.id.navigation_drawer_item_icon);
            mItemLabel = (TextView) itemView.findViewById(R.id.navigation_drawer_item_text);
        }

        public void displayPosition(int position) {
            switch (position) {
                // Set label, icon, and onClick behavior for the row
                case 0: // Go to subreddit
                    mItemLabel.setText(getString(R.string.go_to_subreddit));
                    mItemRow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDrawerLayout.closeDrawer(GravityCompat.START);
//                            String inputSubreddit = mNavToSubredditText.getText().toString();
//                            if (!inputSubreddit.equals("")) {
//                                mNavToSubredditText.setText("");
//                                showSubreddit(inputSubreddit);
//                            }
                        }
                    });
                    break;
                case 1:
                    mItemLabel.setText(getString(R.string.drawer_log_in));
                    mItemRow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDrawerLayout.closeDrawer(GravityCompat.START);
                            openWebViewForURL(RedditAuthProxy.AUTHORIZATION_URL);
                        }
                    });
                    break;
                case 2:
                    mItemLabel.setText(getString(R.string.drawer_user_profile));
                    mItemRow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDrawerLayout.closeDrawer(GravityCompat.START);
                        }
                    });
                    break;
                case 3:
                    mItemLabel.setText(getString(R.string.drawer_front_page));
                    mItemRow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDrawerLayout.closeDrawer(GravityCompat.START);
                            showSubreddit(null);
                        }
                    });
                    break;
                case 4:
                    mItemLabel.setText(getString(R.string.drawer_r_all));
                    mItemRow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDrawerLayout.closeDrawer(GravityCompat.START);
                            showSubreddit("all");
                        }
                    });
                    break;
                case 5:
                    mItemLabel.setText(getString(R.string.drawer_subreddits));
                    mItemRow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDrawerLayout.closeDrawer(GravityCompat.START);
                        }
                    });
                    break;
                case 6:
                    mItemLabel.setText(getString(R.string.drawer_random_subreddit));
                    mItemRow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDrawerLayout.closeDrawer(GravityCompat.START);
                            showSubreddit("random");
                        }
                    });
                    break;
            }
        }
    }
}
