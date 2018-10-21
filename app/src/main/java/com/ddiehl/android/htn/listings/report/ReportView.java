package com.ddiehl.android.htn.listings.report;

import android.app.ProgressDialog;
import android.content.Context;

import com.ddiehl.android.htn.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

import androidx.fragment.app.FragmentManager;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.SingleSubject;
import rxreddit.api.RedditService;
import rxreddit.model.SubredditRule;
import rxreddit.model.SubredditRules;
import timber.log.Timber;


/**
 * Invisible Activity for requesting data from API for reporting a listing,
 * then displaying a dialog with options.
 */
public class ReportView implements ReportDialog.Listener {

    private final Context context;
    private final FragmentManager fragmentManager;
    private final String listingId;
    private final String subreddit;
    private final RedditService redditService;

    private ProgressDialog loadingOverlay;
    private Disposable pendingRequest = Disposables.empty();
    private final SingleSubject<ReportResult> observable = SingleSubject.create();

    public enum ReportResult {
        SUCCESS,
        ERROR,
        CANCELED,
    }

    public ReportView(
            @NotNull Context context,
            @NotNull FragmentManager fragmentManager,
            @NotNull String listingId,
            @Nullable String subreddit,
            @NotNull RedditService redditService) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.listingId = listingId;
        this.subreddit = subreddit;
        this.redditService = redditService;
    }

    public void showSpinner() {
        if (loadingOverlay == null) {
            loadingOverlay = new ProgressDialog(context, R.style.ProgressDialog);
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

    public Single<ReportResult> show() {
        return Single.defer(() -> {
            if (subreddit != null) {
                pendingRequest = getSubredditRules()
                        .doOnSubscribe(disposable -> showSpinner())
                        .doFinally(this::dismissSpinner)
                        .subscribe(this::onSubredditRulesRetrieved, this::onGetSubredditRulesError);
            } else {
                showReportDialogWithRules(null, getSiteRulesList());
            }
            return observable;
        });
    }

    String[] getSiteRulesList() {
        return context.getResources().getStringArray(R.array.report_site_rules);
    }

    Observable<SubredditRules> getSubredditRules() {
        return redditService.getSubredditRules(subreddit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    void onGetSubredditRulesError(Throwable error) {
        if (!(error instanceof IOException)) {
            Timber.e(error, "Error getting subreddit rules");
        }
        observable.onSuccess(ReportResult.ERROR);
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

    void showReportDialogWithRules(@Nullable String[] subredditRules, @NotNull String[] siteRules) {
        ReportDialog.newInstance(subredditRules == null ? new String[] { } : subredditRules, siteRules)
                .show(fragmentManager, ReportDialog.TAG);
    }

    void report(@Nullable String rule, @Nullable String siteRule, @Nullable String other) {
        pendingRequest = redditService.report(listingId, rule, siteRule, other)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> showSpinner())
                .doFinally(this::dismissSpinner)
                .subscribe(this::onReported, this::onReportError);
    }

    private void onReportError(Throwable error) {
        if (!(error instanceof IOException)) {
            Timber.e(error, "Error submitting report");
        }
        observable.onSuccess(ReportResult.ERROR);
    }

    private void onReported() {
        observable.onSuccess(ReportResult.SUCCESS);
    }

    //region ReportDialog.Listener

    @Override
    public void onRuleSubmitted(@NotNull String rule) {
        Timber.i("Report submitted for rule");
        report(rule, null, null);
    }

    @Override
    public void onSiteRuleSubmitted(@NotNull String rule) {
        Timber.i("Report submitted for site rule");
        report(null, rule, null);
    }

    @Override
    public void onOtherSubmitted(@NotNull String reason) {
        Timber.i("Report submitted for other reason");
        // API does not properly save reasons passed in the 'other_reason' field,
        // so just pass it in the 'reason' field
        report(reason, null, null);
    }

    @Override
    public void onCancelled() {
        observable.onSuccess(ReportResult.CANCELED);
        dismiss();
    }

    //endregion

    public void dismiss() {
        dismissSpinner();
        pendingRequest.dispose();
    }
}
