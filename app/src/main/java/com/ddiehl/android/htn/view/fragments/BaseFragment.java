package com.ddiehl.android.htn.view.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.MailTo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.analytics.Analytics;
import com.ddiehl.android.htn.utils.AndroidUtils;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.MenuTintUtils;
import com.ddiehl.android.htn.view.RedditNavigationView;
import com.ddiehl.android.htn.view.activities.SubredditActivity;

import javax.inject.Inject;

import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rxreddit.api.RedditService;
import rxreddit.model.UserAccessToken;

import static android.app.Activity.RESULT_OK;

public abstract class BaseFragment extends Fragment implements MainView {

    protected static final int REQUEST_CHOOSE_SORT = 1;
    protected static final int REQUEST_CHOOSE_TIMESPAN = 2;
    protected static final int REQUEST_NSFW_WARNING = 3;
    protected static final int REQUEST_ADD_COMMENT = 4;
    protected static final int REQUEST_SIGN_IN = 5;

    @Inject protected RedditService mRedditService;
    @Inject protected IdentityManager mIdentityManager;
    @Inject protected Analytics mAnalytics;

    protected RedditNavigationView mRedditNavigationView;
    ProgressDialog mLoadingOverlay;
    protected TabLayout mTabLayout;

    protected abstract int getLayoutResId();
    protected abstract View getChromeView();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HoldTheNarwhal.getApplicationComponent().inject(this);
    }

    @NonNull @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        View view = inflater.inflate(getLayoutResId(), container, false);
        ButterKnife.bind(this, view);
        mTabLayout = ButterKnife.findById(getActivity(), R.id.tab_layout);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RedditNavigationView) {
            mRedditNavigationView = (RedditNavigationView) context;
        }
    }

    @Override
    public void onDetach() {
        mRedditNavigationView = null;
        super.onDetach();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuTintUtils.tintAllIcons(menu, ContextCompat.getColor(getContext(), R.color.icons));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    String url = data.getStringExtra(WebViewFragment.EXTRA_CALLBACK_URL);
                    processAuthenticationCallback(url);
                }
                break;
            case REQUEST_NSFW_WARNING:
                break;
            default:
                break;
        }
    }

    protected void processAuthenticationCallback(String url) {
        mRedditService.processAuthenticationCallback(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        getUserIdentity(),
                        error -> {
                            String message = getString(R.string.error_get_user_identity);
                            showError(error, message);
                        });
    }

    private Action1<UserAccessToken> getUserIdentity() {
        return token -> mRedditService.getUserIdentity()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        user -> {
                            mIdentityManager.saveUserIdentity(user);
                        },
                        error -> {
                            String message = getString(R.string.error_get_user_identity);
                            showError(error, message);
                        });
    }

    @Override
    public void doSendEmail(MailTo mailTo) {
        Intent i = AndroidUtils.getNewEmailIntent(
                mailTo.getTo(), mailTo.getSubject(), mailTo.getBody(), mailTo.getCc());
        startActivity(i);
    }

    private int mDialogCount = 0;

    @Override
    public void showSpinner() {
        mDialogCount++;
        if (mLoadingOverlay == null) {
            mLoadingOverlay = new ProgressDialog(getContext(), R.style.ProgressDialog);
            mLoadingOverlay.setCancelable(false);
            mLoadingOverlay.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
//        mLoadingOverlay.setMessage(message);
        mLoadingOverlay.show();
    }

    @Override
    public void loadImageIntoDrawerHeader(@Nullable String url) {
        mRedditNavigationView.showSubredditImage(url);
    }

    public void finish() {
        // If we're the task root, start the main activity
        if (getActivity().isTaskRoot()) {
            Intent intent = SubredditActivity.getIntent(getContext(), null, null, null);
            startActivity(intent);
        }

        // Finish so we can't get back here
        getActivity().finish();
    }

    protected void finish(int resultCode, Intent data) {
        if (getTargetFragment() != null) {
            getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, data);
        } else {
            getActivity().setResult(resultCode);
            getActivity().finish();
        }
    }

    @Override
    public void dismissSpinner() {
        mDialogCount--;
        if (mDialogCount < 0) mDialogCount = 0;
        if (mLoadingOverlay != null && mLoadingOverlay.isShowing() && mDialogCount == 0) {
            mLoadingOverlay.dismiss();
        }
    }

    @Override
    public void showToast(@NonNull CharSequence msg) {
        Snackbar.make(getChromeView(), msg, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showError(Throwable error, @NonNull CharSequence message) {
        Snackbar.make(getChromeView(), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void setTitle(@StringRes int id) {
        getActivity().setTitle(id);
    }

    @Override
    public void setTitle(@NonNull CharSequence title) {
        getActivity().setTitle(title);
    }
}
