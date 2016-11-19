package com.ddiehl.android.htn.listings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.view.TransparentBaseActivity;

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

    public static final String EXTRA_SUBREDDIT = "EXTRA_SUBREDDIT";

    public static final int RESULT_GET_SUBREDDIT_RULES_ERROR = 10;
    public static final int RESULT_REPORT_ERROR = 11;
    public static final int RESULT_REPORT_SUCCESS = Activity.RESULT_OK;

    static final String[] SITE_RULES = {
            "Spam",
            "Personal and confidential information",
            "Threatening, harrassing, or inciting violence"
    };

    public static Intent getIntent(Context context, @Nullable String subreddit) {
        Intent intent = new Intent(context, ReportActivity.class);

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
            Timber.e(error);
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

    //region ReportDialog.Listener

    @Override
    public void onRuleSubmitted(String rule) {
        Toast.makeText(this, rule, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onSiteRuleSubmitted(String rule) {
        Toast.makeText(this, "Site rule: " + rule, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onOtherSubmitted(String reason) {
        Toast.makeText(this, "Other: " + reason, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onCancelled() {
        finish();
    }

    //endregion
}
