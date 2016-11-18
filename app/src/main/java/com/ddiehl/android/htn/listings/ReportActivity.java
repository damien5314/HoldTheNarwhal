package com.ddiehl.android.htn.listings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.view.TransparentBaseActivity;

import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rxreddit.api.RedditService;
import rxreddit.model.ReportForm;
import rxreddit.model.SubredditRules;


/**
 * Invisible Activity for requesting data from API for reporting a listing,
 * then displaying a dialog with options.
 */
public class ReportActivity extends TransparentBaseActivity
        implements ReportDialog.Listener {

    public static final String EXTRA_LISTING_FULLNAME = "EXTRA_LISTING_FULLNAME";

    public static final int RESULT_GET_REPORT_FORM_ERROR = 10;
    public static final int RESULT_REPORT_ERROR = 11;
    public static final int RESULT_REPORT_SUCCESS = Activity.RESULT_OK;

    static final String[] SITE_RULES = {
            "Spam",
            "Personal and confidential information",
            "Threatening, harrassing, or inciting violence"
    };

    public static Intent getIntent(Context context, String fullname) {
        Intent intent = new Intent(context, ReportActivity.class);
        intent.putExtra(EXTRA_LISTING_FULLNAME, fullname);
        return intent;
    }

    @Inject RedditService mRedditService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HoldTheNarwhal.getApplicationComponent().inject(this);
        setTitle(null);

        getReportDetails();
    }

    String getListingFullname() {
        return getIntent().getStringExtra(EXTRA_LISTING_FULLNAME);
    }

    void getReportDetails() {
        mRedditService.getReportForm(getListingFullname())
                .doOnSubscribe(this::showSpinner)
                .doOnUnsubscribe(this::dismissSpinner)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onReportFormRetrieved(), onGetReportFormError());
    }

    Action1<Throwable> onGetReportFormError() {
        return (error) -> {
            setResult(RESULT_GET_REPORT_FORM_ERROR);
            finish();
        };
    }

    Action1<ReportForm> onReportFormRetrieved() {
        return (form) -> {
            List<SubredditRules.Rule> rules = form.getRules();

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
    }

    @Override
    public void onSiteRuleSubmitted(String rule) {
        Toast.makeText(this, "Site rule: " + rule, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onOtherSubmitted(String reason) {
        Toast.makeText(this, "Other: " + reason, Toast.LENGTH_LONG).show();
    }

    //endregion
}
