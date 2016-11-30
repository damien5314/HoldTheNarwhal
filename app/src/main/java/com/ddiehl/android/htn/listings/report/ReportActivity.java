package com.ddiehl.android.htn.listings.report;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.listings.ReportDialogBuilder;
import com.ddiehl.android.htn.view.TransparentBaseActivity;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rxreddit.api.RedditService;
import rxreddit.model.SubredditRules;
import timber.log.Timber;


/**
 * Invisible Activity for requesting data from API for reporting a listing,
 * then displaying a dialog with options.
 */
public class ReportActivity extends TransparentBaseActivity
        implements ReportDialog.Listener {

    public static final String EXTRA_LISTING_ID = "EXTRA_LISTING_ID";
    public static final String EXTRA_SUBREDDIT = "EXTRA_SUBREDDIT";

    public static final int RESULT_GET_SUBREDDIT_RULES_ERROR = 10;
    public static final int RESULT_REPORT_ERROR = 11;
    public static final int RESULT_REPORT_SUCCESS = Activity.RESULT_OK;

    public static Intent getIntent(
            Context context, @NonNull String listingId, @Nullable String subreddit) {
        Intent intent = new Intent(context, ReportActivity.class);

        intent.putExtra(EXTRA_LISTING_ID, listingId);

        if (subreddit != null) {
            intent.putExtra(EXTRA_SUBREDDIT, subreddit);
        }

        return intent;
    }

    @Inject RedditService mRedditService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HoldTheNarwhal.getApplicationComponent().inject(this);
        setTitle(null);
    }

    @NonNull String getListingId() {
        return getIntent().getStringExtra(EXTRA_LISTING_ID);
    }

    @Nullable String getSubredditName() {
        return getIntent().getStringExtra(EXTRA_SUBREDDIT);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(ReportDialog.TAG);
        if (fragment == null) {
            loadReportDialog();
        }
    }

    void loadReportDialog() {
        if (getSubredditName() != null) {
            getSubredditRules()
                    .doOnSubscribe(this::showSpinner)
                    .doOnUnsubscribe(this::dismissSpinner)
                    .subscribe(onSubredditRulesRetrieved(), onGetSubredditRulesError());
        } else {
            showReportDialogWithRules(null, getSiteRulesList());
        }
    }

    String[] getSiteRulesList() {
        return getResources().getStringArray(R.array.report_site_rules);
    }

    Observable<SubredditRules> getSubredditRules() {
        return mRedditService.getSubredditRules(getSubredditName())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    Action1<Throwable> onGetSubredditRulesError() {
        return (error) -> {
            if (!(error instanceof IOException)) {
                Timber.e(error, "Error getting subreddit rules");
            }
            setResult(RESULT_GET_SUBREDDIT_RULES_ERROR);
            finish();
        };
    }

    Action1<SubredditRules> onSubredditRulesRetrieved() {
        return (result) -> {
            List<SubredditRules.Rule> rules = result.getRules();

            // Get short names for each rule
            String[] ruleNames = new String[rules.size()];
            for (int i = 0; i < rules.size(); i++) {
                ruleNames[i] = rules.get(i).getShortName();
            }

            // Show dialog
            showReportDialogWithRules(ruleNames, getSiteRulesList());
        };
    }

    void showReportDialogWithRules(String[] subredditRules, String[] siteRules) {
        ReportDialog dialog = new ReportDialogBuilder(subredditRules, siteRules)
                .build();

        dialog.show(getSupportFragmentManager(), ReportDialog.TAG);
    }

    void report(String rule, String siteRule, String other) {
        mRedditService.report(getListingId(), rule, siteRule, other)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this::showSpinner)
                .doOnUnsubscribe(this::dismissSpinner)
                .subscribe(onReported(), onReportError());
    }

    private Action1<Throwable> onReportError() {
        return (error) -> {
            if (!(error instanceof IOException)) {
                Timber.e(error, "Error submitting report");
            }
            setResult(RESULT_REPORT_ERROR);
            finish();
        };
    }

    private Action1<Void> onReported() {
        return (result) -> {
            setResult(RESULT_REPORT_SUCCESS);
            finish();
        };
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
        finish();
    }

    //endregion
}
