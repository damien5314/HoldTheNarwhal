package com.ddiehl.android.htn.listings.report;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.TransparentBaseActivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

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
public class ReportActivity extends TransparentBaseActivity
        implements ReportDialog.Listener {

    public static final String EXTRA_LISTING_ID = "EXTRA_LISTING_ID";
    public static final String EXTRA_SUBREDDIT = "EXTRA_SUBREDDIT";

    public static final int RESULT_GET_SUBREDDIT_RULES_ERROR = 10;
    public static final int RESULT_REPORT_ERROR = 11;
    public static final int RESULT_REPORT_SUCCESS = Activity.RESULT_OK;

    public static Intent getIntent(
            Context context, @NotNull String listingId, @Nullable String subreddit) {
        Intent intent = new Intent(context, ReportActivity.class);

        intent.putExtra(EXTRA_LISTING_ID, listingId);

        if (subreddit != null) {
            intent.putExtra(EXTRA_SUBREDDIT, subreddit);
        }

        return intent;
    }

    @Inject RedditService redditService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HoldTheNarwhal.getApplicationComponent().inject(this);
        setTitle(null);
    }

    @NotNull String getListingId() {
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
        setResult(RESULT_GET_SUBREDDIT_RULES_ERROR);
        finish();
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
        ReportDialog dialog = new ReportDialogBuilder(subredditRules, siteRules)
                .build();

        dialog.show(getSupportFragmentManager(), ReportDialog.TAG);
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
        setResult(RESULT_REPORT_ERROR);
        finish();
    }

    private void onReported() {
        setResult(RESULT_REPORT_SUCCESS);
        finish();
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
        report("other", null, reason);
    }

    @Override
    public void onCancelled() {
        finish();
    }

    //endregion
}
