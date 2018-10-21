package com.ddiehl.android.htn.listings.report;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import rxreddit.api.RedditService;
import rxreddit.model.SubredditRule;
import rxreddit.model.SubredditRules;
import timber.log.Timber;


/**
 * Invisible Activity for requesting data from API for reporting a listing,
 * then displaying a dialog with options.
 */
public class ReportView extends DialogFragment
        implements ReportDialog.Listener {

    public static final String TAG = "ReportView";
    public static final String EXTRA_LISTING_ID = "EXTRA_LISTING_ID";
    public static final String EXTRA_SUBREDDIT = "EXTRA_SUBREDDIT";

    public static final int RESULT_GET_SUBREDDIT_RULES_ERROR = 10;
    public static final int RESULT_REPORT_ERROR = 11;
    public static final int RESULT_REPORT_SUCCESS = Activity.RESULT_OK;
    public static final int RESULT_REPORT_CANCELED = Activity.RESULT_CANCELED;

    public static ReportView newInstance(
            @NotNull String listingId,
            @Nullable String subreddit) {
        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_LISTING_ID, listingId);
        if (subreddit != null) {
            arguments.putString(EXTRA_SUBREDDIT, subreddit);
        }
        ReportView fragment = new ReportView();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Inject RedditService redditService;

    @Override
    public void onCreate(@androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HoldTheNarwhal.getApplicationComponent().inject(this);
    }

    @NotNull String getListingId() {
        return getArguments().getString(EXTRA_LISTING_ID);
    }

    @Nullable String getSubredditName() {
        return getArguments().getString(EXTRA_SUBREDDIT);
    }

    @Override
    public void onStart() {
        super.onStart();
        Fragment fragment = getChildFragmentManager().findFragmentByTag(ReportDialog.TAG);
        if (fragment == null) {
            loadReportDialog();
        }
    }

    void loadReportDialog() {
        if (getSubredditName() != null) {
            getSubredditRules()
                    .doOnSubscribe(disposable -> showSpinner())
                    .doFinally(this::dismissSpinner)
                    .subscribe(this::onSubredditRulesRetrieved, this::onGetSubredditRulesError);
        } else {
            showReportDialogWithRules(null, getSiteRulesList());
        }
    }

    String[] getSiteRulesList() {
        return getResources().getStringArray(R.array.report_site_rules);
    }

    Observable<SubredditRules> getSubredditRules() {
        return redditService.getSubredditRules(getSubredditName())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    void onGetSubredditRulesError(Throwable error) {
        if (!(error instanceof IOException)) {
            Timber.e(error, "Error getting subreddit rules");
        }
        deliverResult(RESULT_GET_SUBREDDIT_RULES_ERROR);
        dismiss();
    }

    void onSubredditRulesRetrieved(SubredditRules result) {
        List<SubredditRule> rules = result.getRules();

        // Get short names for each rule
        String[] ruleNames = new String[rules.size()];
        for (int i = 0; i < rules.size(); i++) {
            ruleNames[i] = rules.get(i).getShortName();
        }

        // Show dialog
        showReportDialogWithRules(ruleNames, getSiteRulesList());
    }

    void showReportDialogWithRules(String[] subredditRules, String[] siteRules) {
        ReportDialog.newInstance(subredditRules, siteRules)
                .show(getChildFragmentManager(), ReportDialog.TAG);
    }

    void report(String rule, String siteRule, String other) {
        redditService.report(getListingId(), rule, siteRule, other)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> showSpinner())
                .doOnDispose(this::dismissSpinner)
                .subscribe(this::onReported, this::onReportError);
    }

    private void onReportError(Throwable error) {
        if (!(error instanceof IOException)) {
            Timber.e(error, "Error submitting report");
        }
        deliverResult(RESULT_REPORT_ERROR);
        dismiss();
    }

    private void onReported() {
        deliverResult(RESULT_REPORT_SUCCESS);
        dismiss();
    }

    private void deliverResult(int resultCode) {
        final Fragment targetFragment = getTargetFragment();
        if (targetFragment == null) {
            throw new IllegalStateException("No target fragment set for ReportView");
        }
        targetFragment.onActivityResult(getTargetRequestCode(), resultCode, null);
    }

    //region ReportDialog.Listener

    @Override
    public void onRuleSubmitted(String rule) {
        Timber.i("Report submitted for rule");
        report(rule, null, null);
    }

    @Override
    public void onSiteRuleSubmitted(String rule) {
        Timber.i("Report submitted for site rule");
        report(null, rule, null);
    }

    @Override
    public void onOtherSubmitted(String reason) {
        Timber.i("Report submitted for other reason");
        // API does not properly save reasons passed in the 'other_reason' field,
        // so just pass it in the 'reason' field
        report(reason, null, null);
    }

    @Override
    public void onCancelled() {
        deliverResult(RESULT_REPORT_CANCELED);
        dismiss();
    }

    //endregion

    private ProgressDialog loadingOverlay;

    public void showSpinner() {
        if (loadingOverlay == null) {
            loadingOverlay = new ProgressDialog(requireContext(), R.style.ProgressDialog);
            loadingOverlay.setCancelable(false);
            loadingOverlay.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        loadingOverlay.show();
    }

    public void dismissSpinner() {
        if (loadingOverlay != null && loadingOverlay.isShowing()) {
            loadingOverlay.dismiss();
        }
    }
}
