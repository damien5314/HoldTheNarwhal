package com.ddiehl.android.htn.view.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ddiehl.android.htn.BusProvider;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.events.responses.UserAuthCodeReceivedEvent;
import com.ddiehl.android.htn.io.RedditServiceAuth;
import com.ddiehl.android.htn.presenter.MainPresenter;
import com.ddiehl.android.htn.presenter.MainPresenterImpl;
import com.ddiehl.android.htn.utils.BaseUtils;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.SettingsChangedListener;
import com.ddiehl.android.htn.view.dialogs.ConfirmSignOutDialog;
import com.ddiehl.android.htn.view.fragments.SubredditFragment;
import com.ddiehl.android.htn.view.fragments.UserProfileFragment;
import com.ddiehl.android.htn.view.fragments.WebViewFragment;
import com.ddiehl.reddit.identity.UserIdentity;
import com.flurry.android.FlurryAgent;
import com.mopub.common.MoPub;
import com.mopub.mobileads.MoPubConversionTracker;
import com.squareup.otto.Bus;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity
        implements MainView, ConfirmSignOutDialog.Callbacks, NavigationView.OnNavigationItemSelectedListener {

    private static final String DIALOG_CONFIRM_SIGN_OUT = "dialog_confirm_sign_out";

    private static final int REQUEST_CODE_SETTINGS = 1001;

    private MainPresenter mMainPresenter;
    private Bus mBus = BusProvider.getInstance();
    private String mLastAuthCode;

    private ProgressDialog mProgressBar;
    private Dialog mSubredditNavigationDialog;

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

        mMainPresenter = new MainPresenterImpl(this, this);
        updateUserIdentity();
        mNavigationView.setNavigationItemSelectedListener(this);

        // MoPub configuration
        new MoPubConversionTracker().reportAppOpen(this);
        MoPub.setLocationAwareness(MoPub.LocationAwareness.DISABLED);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this);
        mBus.register(mMainPresenter);

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment == null) {
            showSubreddit(null);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
        mBus.unregister(mMainPresenter);
    }

    @Override
    public void updateUserIdentity() {
        UserIdentity identity = mMainPresenter.getAuthorizedUser();
        mAccountNameView.setText(identity == null ?
                getString(R.string.account_name_unauthorized) : identity.getName());
        mSignOutView.setVisibility(identity == null ? View.GONE : View.VISIBLE);
        mGoldIndicator.setVisibility(identity != null && identity.isGold() ? View.VISIBLE : View.GONE);
        updateNavigationItems();
        FlurryAgent.setUserId(identity == null ? null : BaseUtils.getMd5HexString(identity.getName()));
    }

    @Override
    public void closeNavigationDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private void updateNavigationItems() {
        Menu menu = mNavigationView.getMenu();
        UserIdentity user = mMainPresenter.getAuthorizedUser();
        boolean b = user != null && user.getName() != null;
        menu.findItem(R.id.drawer_log_in).setVisible(!b);
        menu.findItem(R.id.drawer_user_profile).setVisible(b);
        menu.findItem(R.id.drawer_subreddits).setVisible(b);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.drawer_navigate_to_subreddit:
                showSubredditNavigationDialog();
                FlurryAgent.logEvent("nav drawer - navigate to subreddit");
                return true;
            case R.id.drawer_log_in:
                showLoginView();
                FlurryAgent.logEvent("nav drawer - log in");
                return true;
            case R.id.drawer_user_profile:
                showUserProfile();
                FlurryAgent.logEvent("nav drawer - user profile");
                return true;
            case R.id.drawer_subreddits:
                showUserSubreddits();
                FlurryAgent.logEvent("nav drawer - user subreddits");
                return true;
            case R.id.drawer_front_page:
                showSubreddit(null);
                FlurryAgent.logEvent("nav drawer - navigate to front page");
                return true;
            case R.id.drawer_r_all:
                showSubreddit("all");
                FlurryAgent.logEvent("nav drawer - navigate to /r/all");
                return true;
            case R.id.drawer_random_subreddit:
                showSubreddit("random");
                FlurryAgent.logEvent("nav drawer - navigate to random subreddit");
                return true;
        }
        return false;
    }

    @Override
    public void showLoginView() {
        closeNavigationDrawer();
        showWebViewForURL(RedditServiceAuth.AUTHORIZATION_URL);
    }

    @Override
    public void showUserProfile() {
        String username = mMainPresenter.getAuthorizedUser().getName();
        showUserProfile("overview", username);
    }

    @Override
    public void showUserProfile(String show, String username) {
        closeNavigationDrawer();
        mMainPresenter.setUsernameContext(username);
        FragmentManager fm = getSupportFragmentManager();
        Fragment f = UserProfileFragment.newInstance(show, username);
        fm.beginTransaction().replace(R.id.fragment_container, f)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void showSubreddit(String subreddit) {
        closeNavigationDrawer();
        FragmentManager fm = getSupportFragmentManager();
        Fragment f = SubredditFragment.newInstance(subreddit);
        FragmentTransaction ft = fm.beginTransaction().replace(R.id.fragment_container, f);

        Fragment cf = fm.findFragmentById(R.id.fragment_container);
        if (cf != null) {
            ft.addToBackStack(null);
        }

        ft.commit();
    }

    @Override
    public void showWebViewForURL(String url) {
        closeNavigationDrawer();
        FragmentManager fm = getSupportFragmentManager();
        Fragment f = WebViewFragment.newInstance(url);
        fm.beginTransaction().replace(R.id.fragment_container, f)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void showUserSubreddits() {
        showToast(R.string.implementation_pending);
    }

    @OnClick(R.id.sign_out_button)
    void onSignOut() {
        ConfirmSignOutDialog dialog = ConfirmSignOutDialog.newInstance();
        dialog.show(getSupportFragmentManager(), DIALOG_CONFIRM_SIGN_OUT);
        FlurryAgent.logEvent("clicked sign out");
    }

    private void showSubredditNavigationDialog() {
        if (mSubredditNavigationDialog == null) {
            mSubredditNavigationDialog = new Dialog(this);
            mSubredditNavigationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mSubredditNavigationDialog.setContentView(R.layout.navigate_to_subreddit_edit_text);
            ButterKnife.findById(mSubredditNavigationDialog, R.id.drawer_navigate_to_subreddit_go)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditText vInput = ButterKnife.findById(mSubredditNavigationDialog,
                                    R.id.drawer_navigate_to_subreddit_text);
                            String inputSubreddit = vInput.getText().toString();
                            if (inputSubreddit.equals("")) return;

                            inputSubreddit = inputSubreddit.substring(3);
                            inputSubreddit = inputSubreddit.trim();
                            vInput.setText("");
                            mSubredditNavigationDialog.dismiss();
                            showSubreddit(inputSubreddit);
                        }
                    });
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        mSubredditNavigationDialog.show();
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