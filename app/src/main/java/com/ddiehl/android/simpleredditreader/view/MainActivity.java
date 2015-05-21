package com.ddiehl.android.simpleredditreader.view;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.RedditPreferences;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.events.requests.GetSavedUserIdentityEvent;
import com.ddiehl.android.simpleredditreader.events.requests.UserSignOutEvent;
import com.ddiehl.android.simpleredditreader.events.responses.SavedUserIdentityRetrievedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.UserIdentitySavedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.UserSignedOutEvent;
import com.ddiehl.android.simpleredditreader.io.RedditService;
import com.ddiehl.android.simpleredditreader.io.RedditServiceAuth;
import com.ddiehl.reddit.identity.UserIdentity;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class MainActivity extends ActionBarActivity implements MainView {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String DIALOG_CONFIRM_SIGN_OUT = "dialog_confirm_sign_out";

    private Bus mBus;

    // Navigation drawer
    private View mNavigationDrawer;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private RecyclerView mNavigationDrawerListView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mLayoutAdapter;
    private TextView mAccountNameView;
    private View mSignOutView;

    private ProgressDialog mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBus = BusProvider.getInstance();
        mBus.register(this);

        RedditPreferences prefs = RedditPreferences.getInstance(this);
        mBus.register(prefs);

        RedditService authProxy = RedditServiceAuth.getInstance(this);
        mBus.register(authProxy);

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

        // Set onClick to null to intercept click events from background
        mNavigationDrawer = findViewById(R.id.navigation_drawer);
        mNavigationDrawer.setOnClickListener(null);

        mAccountNameView = (TextView) findViewById(R.id.account_name);
        mSignOutView = findViewById(R.id.sign_out_button);
        mSignOutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmSignOutDialog dialog = ConfirmSignOutDialog.newInstance();
                dialog.show(getSupportFragmentManager(), DIALOG_CONFIRM_SIGN_OUT);
            }
        });
        mBus.post(new GetSavedUserIdentityEvent());
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

    @Subscribe
    public void onSavedUserIdentityRetrieved(SavedUserIdentityRetrievedEvent event) {
        UserIdentity identity = event.getUserIdentity();
        updateAccountNameView(identity);
    }

    @Subscribe
    public void onUserIdentitySaved(UserIdentitySavedEvent event) {
        UserIdentity identity = event.getUserIdentity();
        updateAccountNameView(identity);
    }

    @Subscribe
    public void onUserSignedOut(UserSignedOutEvent event) {
        updateAccountNameView(null);
    }

    private void updateAccountNameView(UserIdentity identity) {
        String name = identity == null ? getString(R.string.account_name_unauthorized) : identity.getName();
        mAccountNameView.setText(name);
        mSignOutView.setVisibility(identity == null ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onUserSignOut() {
        mBus.post(new UserSignOutEvent());
    }

    private class NavDrawerItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_EDIT_TEXT = 0;
        private static final int TYPE_TEXT_VIEW = 1;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_EDIT_TEXT:
                    View v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.navigation_drawer_edit_text_row, parent, false);
                    return new NavEditTextViewHolder(v);
                case TYPE_TEXT_VIEW:
                    v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.navigation_drawer_text_view_row, parent, false);
                    return new NavTextViewHolder(v);
                default:
                    throw new RuntimeException("Unexpected ViewHolder type: " + viewType);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof NavEditTextViewHolder) {
                ((NavEditTextViewHolder) holder).bind();
            } else if (holder instanceof NavTextViewHolder) {
                ((NavTextViewHolder) holder).bind(position - 1);
            }
        }

        @Override
        public int getItemCount() {
            return 7;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0)
                return TYPE_EDIT_TEXT;
            return TYPE_TEXT_VIEW;
        }

        private class NavEditTextViewHolder extends RecyclerView.ViewHolder {
            private EditText mEditText;
            private View mSubmitView;

            public NavEditTextViewHolder(View itemView) {
                super(itemView);
                mEditText = (EditText) itemView.findViewById(R.id.drawer_navigate_to_subreddit_text);
                mSubmitView = itemView.findViewById(R.id.drawer_navigate_to_subreddit_go);

                mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus) { // Hide keyboard
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                    }
                });
                mEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            mSubmitView.performClick();
                            return true;
                        }
                        return false;
                    }
                });
            }

            public void bind() {
                mSubmitView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        String inputSubreddit = mEditText.getText().toString();
                        inputSubreddit = inputSubreddit.trim();
                        if (!inputSubreddit.equals("")) {
                            mEditText.setText("");
                            showSubreddit(inputSubreddit);
                        }
                    }
                });
            }
        }

        private class NavTextViewHolder extends RecyclerView.ViewHolder {
            private View mItemRow;
            private ImageView mItemIcon;
            private TextView mItemLabel;

            public NavTextViewHolder(View itemView) {
                super(itemView);
                mItemRow = itemView.findViewById(R.id.navigation_drawer_item);
                mItemIcon = (ImageView) itemView.findViewById(R.id.navigation_drawer_item_icon);
                mItemLabel = (TextView) itemView.findViewById(R.id.navigation_drawer_item_text);
            }

            public void bind(int position) {
                switch (position) {
                    // Set label, icon, and onClick behavior for the row
                    case 0:
                        mItemLabel.setText(getString(R.string.drawer_log_in));
                        mItemRow.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDrawerLayout.closeDrawer(GravityCompat.START);
                                openWebViewForURL(RedditServiceAuth.AUTHORIZATION_URL);
                            }
                        });
                        break;
                    case 1:
                        mItemLabel.setText(getString(R.string.drawer_user_profile));
                        mItemRow.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDrawerLayout.closeDrawer(GravityCompat.START);
                            }
                        });
                        break;
                    case 2:
                        mItemLabel.setText(getString(R.string.drawer_subreddits));
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
}
