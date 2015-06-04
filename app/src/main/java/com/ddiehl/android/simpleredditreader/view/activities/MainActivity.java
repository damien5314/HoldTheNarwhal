package com.ddiehl.android.simpleredditreader.view.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.BusProvider;
import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.events.responses.UserAuthCodeReceivedEvent;
import com.ddiehl.android.simpleredditreader.presenter.MainPresenter;
import com.ddiehl.android.simpleredditreader.presenter.MainPresenterImpl;
import com.ddiehl.android.simpleredditreader.view.MainView;
import com.ddiehl.android.simpleredditreader.view.SettingsChangedListener;
import com.ddiehl.android.simpleredditreader.view.dialogs.ConfirmSignOutDialog;
import com.ddiehl.android.simpleredditreader.view.fragments.LinksFragment;
import com.ddiehl.android.simpleredditreader.view.fragments.UserOverviewFragment;
import com.ddiehl.android.simpleredditreader.view.fragments.WebViewFragment;
import com.ddiehl.reddit.identity.UserIdentity;
import com.squareup.otto.Bus;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends ActionBarActivity implements MainView, ConfirmSignOutDialog.Callbacks {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String DIALOG_CONFIRM_SIGN_OUT = "dialog_confirm_sign_out";

    private static final int REQUEST_CODE_SETTINGS = 1001;

    private MainPresenter mMainPresenter;
    private Bus mBus = BusProvider.getInstance();
    private String mLastAuthCode;

    private ProgressDialog mProgressBar;
    private Dialog mSubredditNavigationDialog;

    @InjectView(R.id.navigation_tabs) TabLayout mTabLayout;
    @InjectView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @InjectView(R.id.navigation_view) NavigationView mNavigationView;
    @InjectView(R.id.user_account_icon) ImageView mGoldIndicator;
    @InjectView(R.id.account_name) TextView mAccountNameView;
    @InjectView(R.id.sign_out_button) View mSignOutView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_navigation_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ButterKnife.inject(this);



        // Set up navigation tabs
        mTabLayout.setVisibility(View.GONE);
        mTabLayout.addTab(mTabLayout.newTab().setText("overview"));
        mTabLayout.addTab(mTabLayout.newTab().setText("comments"));
        mTabLayout.addTab(mTabLayout.newTab().setText("submitted"));
        mTabLayout.addTab(mTabLayout.newTab().setText("gilded"));
        mTabLayout.addTab(mTabLayout.newTab().setText("upvoted"));
        mTabLayout.addTab(mTabLayout.newTab().setText("downvoted"));
        mTabLayout.addTab(mTabLayout.newTab().setText("hidden"));
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, "Tab selected: " + tab.getPosition() + " " + tab.getText());
            }
        });

        // Set up navigation drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mMainPresenter = new MainPresenterImpl(this, this);
        updateNavigationItems();

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.drawer_navigate_to_subreddit:
                        showSubredditNavigationDialog();
                        return true;
                    case R.id.drawer_log_in:
                        mMainPresenter.presentLoginView();
                        return true;
                    case R.id.drawer_user_profile:
                        mMainPresenter.showUserProfile(null);
                        return true;
                    case R.id.drawer_subreddits:
                        mMainPresenter.showUserSubreddits();
                        return true;
                    case R.id.drawer_front_page:
                        mMainPresenter.showSubreddit(null);
                        return true;
                    case R.id.drawer_r_all:
                        mMainPresenter.showSubreddit("all");
                        return true;
                    case R.id.drawer_random_subreddit:
                        mMainPresenter.showSubreddit("random");
                        return true;
                }
                return false;
            }
        });
    }

    @OnClick(R.id.sign_out_button)
    void onSignOut() {
        ConfirmSignOutDialog dialog = ConfirmSignOutDialog.newInstance();
        dialog.show(getSupportFragmentManager(), DIALOG_CONFIRM_SIGN_OUT);
    }

    private void showSubredditNavigationDialog() {
        if (mSubredditNavigationDialog == null) {
            mSubredditNavigationDialog = new Dialog(this);
            mSubredditNavigationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mSubredditNavigationDialog.setContentView(R.layout.navigation_drawer_edit_text_row);
            ButterKnife.findById(mSubredditNavigationDialog, R.id.drawer_navigate_to_subreddit_go)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditText vInput = ButterKnife.findById(mSubredditNavigationDialog, R.id.drawer_navigate_to_subreddit_text);
                            String inputSubreddit = vInput.getText().toString();
                            if (inputSubreddit.equals("")) return;

                            inputSubreddit = inputSubreddit.substring(3);
                            inputSubreddit = inputSubreddit.trim();
                            vInput.setText("");
                            mSubredditNavigationDialog.dismiss();
                            mMainPresenter.showSubreddit(inputSubreddit);
                        }
                    });
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        mSubredditNavigationDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBus.register(mMainPresenter);

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment == null) {
            showSubreddit(null);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBus.unregister(mMainPresenter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return false;
    }

    @Override
    public void updateNavigationItems() {
        Menu menu = mNavigationView.getMenu();
        UserIdentity user = mMainPresenter.getAuthorizedUser();
        boolean b = user != null && user.getName() != null;
        menu.findItem(R.id.drawer_log_in).setVisible(!b);
        menu.findItem(R.id.drawer_user_profile).setVisible(b);
        menu.findItem(R.id.drawer_subreddits).setVisible(b);
    }

    @Override
    public void showSpinner(String message) {
        if (mProgressBar == null) {
            mProgressBar = new ProgressDialog(this, R.style.ProgressDialog);
            mProgressBar.setCancelable(false);
            mProgressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        mProgressBar.setMessage(message);
        mProgressBar.show();
    }

    @Override
    public void showSpinner(int resId) {
        showSpinner(getString(resId));
    }

    @Override
    public void dismissSpinner() {
        if (mProgressBar != null && mProgressBar.isShowing()) {
            mProgressBar.dismiss();
        }
    }

    @Override
    public void showToast(int resId) {
        showToast(getString(resId));
    }

    @Override
    public void showToast(String s) {
        Snackbar.make(mDrawerLayout, s, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void closeNavigationDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public void setAccount(UserIdentity identity) {
        mAccountNameView.setText(identity == null ?
                getString(R.string.account_name_unauthorized) : identity.getName());
        mSignOutView.setVisibility(identity == null ? View.GONE : View.VISIBLE);
        mGoldIndicator.setVisibility(identity != null && identity.isGold() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showUserProfile(String userId) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        FragmentManager fm = getSupportFragmentManager();
        Fragment f = UserOverviewFragment.newInstance(userId);
        fm.beginTransaction().replace(R.id.fragment_container, f)
                .addToBackStack(null)
                .commit();
        mTabLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void showSubreddit(String subreddit) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        FragmentManager fm = getSupportFragmentManager();
        Fragment currentFragment = fm.findFragmentById(R.id.fragment_container);
        // If the current fragment is a LinksFragment, just update the subreddit
        // Else, swap in a LinksFragment
        if (currentFragment instanceof LinksFragment) {
            ((LinksFragment) currentFragment).updateSubreddit(subreddit);
        } else {
            Fragment f = LinksFragment.newInstance(subreddit);
            fm.beginTransaction().replace(R.id.fragment_container, f)
                    .addToBackStack(null)
                    .commit();
            mTabLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void openWebViewForURL(String url) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment f = WebViewFragment.newInstance(url);
        fm.beginTransaction().replace(R.id.fragment_container, f)
                .addToBackStack(null)
                .commit();
        mTabLayout.setVisibility(View.GONE);
    }

    @Override
    public void onUserAuthCodeReceived(String authCode) {
        // Fix for API 10; authorization page was loading twice with same auth code
        if (authCode.equals(mLastAuthCode))
            return;
        mLastAuthCode = authCode;

        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack();

        // Notify auth API about the auth code retrieval
        mBus.post(new UserAuthCodeReceivedEvent(authCode));
    }

    @Override
    public void onSignOutConfirm() {
        mMainPresenter.signOutUser();
    }

    @Override
    public void onSignOutCancel() {
        // Do nothing
    }

    public void showSettings() {
        startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_CODE_SETTINGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SETTINGS:
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (f instanceof SettingsChangedListener) {
                    SettingsChangedListener l = (SettingsChangedListener) f;
                    l.onSettingsChanged();
                }
                break;
            default:
        }
    }
}
