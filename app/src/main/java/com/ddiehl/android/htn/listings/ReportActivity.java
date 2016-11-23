package com.ddiehl.android.htn.listings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.ddiehl.android.htn.HoldTheNarwhal;
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

    static final String[] SITE_RULES = {
            "Spam",
            "Personal and confidential information",
            "Threatening, harrassing, or inciting violence"
    };

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
                    .subscribe(onSubredditRulesRetrieved(), onGetSubredditRulesError());
        } else {
            showReportDialogWithRules(null, SITE_RULES);
        }
    }

    Observable<SubredditRules> getSubredditRules() {
        showSpinner();
        return mRedditService.getSubredditRules(getSubredditName())
//                .doOnSubscribe(this::showSpinner)
                .doOnUnsubscribe(this::dismissSpinner)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    Action1<Throwable> onGetSubredditRulesError() {
        return (error) -> {
            if (error instanceof IOException) {
                Timber.w(error);
            } else {
                Timber.e(error);
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
            showReportDialogWithRules(ruleNames, SITE_RULES);
        };
    }

    void showReportDialogWithRules(String[] subredditRules, String[] siteRules) {
        ReportDialog dialog = new ReportDialogBuilder(subredditRules, siteRules)
                .build();

        dialog.show(getSupportFragmentManager(), ReportDialog.TAG);
    }

    void report(String rule, String siteRule, String other) {
        showSpinner();
        mRedditService.report(getListingId(), rule, siteRule, other)
//                .doOnSubscribe(this::showSpinner)
                .doOnUnsubscribe(this::dismissSpinner)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onReported(), onReportError());
    }

    private Action1<Throwable> onReportError() {
        return (error) -> {
            Timber.d(error, "Listing report error");
            setResult(RESULT_REPORT_SUCCESS);
            finish();
        };
    }

    private Action1<Void> onReported() {
        return (result) -> {
            Timber.d("Listing report successful");
            setResult(RESULT_REPORT_ERROR);
            finish();
        };
    }

    //region ReportDialog.Listener

    @Override
    public void onRuleSubmitted(String rule) {
        report(rule, null, null);
    }

    @Override
    public void onSiteRuleSubmitted(String rule) {
        report(null, rule, null);
    }

    @Override
    public void onOtherSubmitted(String reason) {
        report(null, null, reason);
    }

    @Override
    public void onCancelled() {
        finish();
    }

    //endregion
}
