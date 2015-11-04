package com.ddiehl.android.htn.presenter;

import com.ddiehl.android.htn.AndroidContextProvider;
import com.ddiehl.android.htn.BusProvider;
import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.SettingsManager;
import com.ddiehl.android.htn.analytics.Analytics;
import com.ddiehl.android.htn.events.requests.UserSignOutEvent;
import com.ddiehl.android.htn.events.responses.UserIdentitySavedEvent;
import com.ddiehl.android.htn.logging.Logger;
import com.ddiehl.android.htn.utils.BaseUtils;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.reddit.identity.UserIdentity;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import retrofit.Response;

public class MainPresenterImpl implements MainPresenter {
    private Logger mLogger = HoldTheNarwhal.getLogger();
    private Bus mBus = BusProvider.getInstance();
    private IdentityManager mIdentityManager = HoldTheNarwhal.getIdentityManager();
    private SettingsManager mSettingsManager = HoldTheNarwhal.getSettingsManager();
    private Analytics mAnalytics = HoldTheNarwhal.getAnalytics();

    private MainView mMainView;
    private String mUsernameContext;

    public MainPresenterImpl(MainView view) {
        mMainView = view;
    }

    @Override
    public void onResume() {
        mBus.register(this);
        UserIdentity user = getAuthorizedUser();
        mMainView.updateUserIdentity(user);
        mAnalytics.setUserIdentity(user == null ? null : user.getName());

        boolean b = user != null && user.getName() != null;
        mMainView.updateNavigationItems(b);

        if (!showAnalyticsRequestIfNeverShown()) {
            mAnalytics.startSession();
            mMainView.showSubredditIfEmpty(null);
        }
    }

    @Override
    public void onPause() {
        mAnalytics.endSession();
        mBus.unregister(this);
    }

    @Override
    public void signOutUser() {
        mMainView.closeNavigationDrawer();
        mBus.post(new UserSignOutEvent());
        mAnalytics.logSignOut();
    }

    @Override
    public String getUsernameContext() {
        return mUsernameContext;
    }

    @Override
    public void setUsernameContext(String username) {
        mUsernameContext = username;
    }

    private boolean showAnalyticsRequestIfNeverShown() {
        if (!mSettingsManager.askedForAnalytics()) {
            mMainView.showAnalyticsRequestDialog();
            return true;
        }
        return false;
    }

    @Override
    public void onAnalyticsAccepted() {
        mSettingsManager.setAskedForAnalytics(true);
        mSettingsManager.setAnalyticsEnabled(true);
        mAnalytics.startSession();
        mMainView.showSubredditIfEmpty(null);
    }

    @Override
    public void onAnalyticsDeclined() {
        mSettingsManager.setAskedForAnalytics(true);
        mSettingsManager.setAnalyticsEnabled(false);
        mAnalytics.endSession();
        mMainView.showSubredditIfEmpty(null);
    }

    @Override
    public boolean customTabsEnabled() {
        return mSettingsManager.customTabsEnabled();
    }

    @Subscribe @SuppressWarnings("unused")
    public void onNetworkError(Response error) {
        mLogger.e("Retrofit Error: " + error.raw().message());
//        Log.e("HTN", Log.getStackTraceString(error));
        mMainView.showToast(BaseUtils.getFriendlyError(error));
        mAnalytics.logApiError(error);
    }

    @Subscribe @SuppressWarnings("unused")
    public void onUserIdentitySaved(UserIdentitySavedEvent event) {
        UserIdentity user = event.getUserIdentity();
        mMainView.updateUserIdentity(user);
        if (user != null) {
            // FIXME Ensure we only show this when the user changes
            String name = user.getName();
            String toast = String.format(
                    AndroidContextProvider.getContext().getString(R.string.welcome_user), name);
            mMainView.showToast(toast);
        }
    }

    @Subscribe @SuppressWarnings("unused")
    public void onUserSignOut(UserSignOutEvent event) {
        mMainView.showToast(R.string.user_signed_out);
    }

    private UserIdentity getAuthorizedUser() {
        return mIdentityManager.getUserIdentity();
    }
}
