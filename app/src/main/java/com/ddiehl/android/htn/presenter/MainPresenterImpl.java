package com.ddiehl.android.htn.presenter;

import com.ddiehl.android.htn.AccessTokenManager;
import com.ddiehl.android.htn.AndroidContextProvider;
import com.ddiehl.android.htn.BusProvider;
import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.SettingsManager;
import com.ddiehl.android.htn.analytics.Analytics;
import com.ddiehl.android.htn.io.RedditAuthService;
import com.ddiehl.android.htn.io.RedditService;
import com.ddiehl.android.htn.logging.Logger;
import com.ddiehl.android.htn.utils.BaseUtils;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.SignInListener;
import com.ddiehl.reddit.identity.AccessToken;
import com.ddiehl.reddit.identity.AuthorizationResponse;
import com.ddiehl.reddit.identity.UserAccessToken;
import com.ddiehl.reddit.identity.UserIdentity;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Date;

import retrofit.Response;
import rx.functions.Action0;
import rx.functions.Func1;

public class MainPresenterImpl implements MainPresenter {
    private Logger mLogger = HoldTheNarwhal.getLogger();
    private Bus mBus = BusProvider.getInstance();
    protected RedditService mRedditService = HoldTheNarwhal.getRedditService();
    protected RedditAuthService mRedditAuthService = HoldTheNarwhal.getRedditServiceAuth();
    private AccessTokenManager mAccessTokenManager = HoldTheNarwhal.getAccessTokenManager();
    private IdentityManager mIdentityManager = HoldTheNarwhal.getIdentityManager();
    private SettingsManager mSettingsManager = HoldTheNarwhal.getSettingsManager();
    private Analytics mAnalytics = HoldTheNarwhal.getAnalytics();

    private MainView mMainView;
    private SignInListener mSignInListener;
    private String mUsernameContext;

    public MainPresenterImpl(MainView view, SignInListener signInListener) {
        mMainView = view;
        mSignInListener = signInListener;
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
        mAccessTokenManager.clearSavedUserAccessToken();
        mMainView.showToast(R.string.user_signed_out);
        if (mSignInListener != null) mSignInListener.onUserSignedIn(false);
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

    @Override
    public void onAuthCodeReceived(String authCode) {
        mIdentityManager.clearSavedUserIdentity();
        String grantType = "authorization_code";
        mRedditAuthService.getUserAccessToken(grantType, authCode, RedditAuthService.REDIRECT_URI)
                .map(responseToAccessToken())
                .subscribe(response -> {
                    mAccessTokenManager.saveUserAccessToken().call(response);
                    getUserIdentity().call();
                });
    }

    @Override
    public Action0 getUserIdentity() {
        return () -> mRedditService.getUserIdentity()
                .subscribe(identity -> {
                    mIdentityManager.saveUserIdentity(identity);
                    mMainView.updateUserIdentity(identity);
                    if (identity != null) {
                        // FIXME Ensure we only show this when the user changes
                        String name = identity.getName();
                        String toast = String.format(
                                AndroidContextProvider.getContext().getString(R.string.welcome_user),
                                name);
                        mMainView.showToast(toast);
                    }
                });
    }

    private Func1<AuthorizationResponse, AccessToken> responseToAccessToken() {
        return response -> {
            AccessToken token = new UserAccessToken();
            token.setToken(response.getToken());
            token.setTokenType(response.getToken());
            token.setExpiration(response.getExpiresIn() * 1000 + new Date().getTime());
            token.setScope(response.getScope());
            token.setRefreshToken(response.getRefreshToken());
            return token;
        };
    }

    @Subscribe @SuppressWarnings("unused")
    public void onNetworkError(Response error) {
        mLogger.e("Retrofit Error: " + error.raw().message());
//        Log.e("HTN", Log.getStackTraceString(error));
        mMainView.showToast(BaseUtils.getFriendlyError(error));
        mAnalytics.logApiError(error);
    }

    private UserIdentity getAuthorizedUser() {
        return mIdentityManager.getUserIdentity();
    }
}
