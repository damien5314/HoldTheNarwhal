package com.ddiehl.android.htn.listings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.view.TransparentBaseActivity;

import javax.inject.Inject;

import rxreddit.api.RedditService;


/**
 * Invisible Activity for requesting data from API for reporting a listing,
 * then displaying a dialog with options.
 */
public class ReportActivity extends TransparentBaseActivity {

    public static final String EXTRA_LISTING_FULLNAME = "EXTRA_LISTING_FULLNAME";

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
        showSpinner();
    }
}
