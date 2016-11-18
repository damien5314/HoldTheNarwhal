package com.ddiehl.android.htn.listings;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.ddiehl.android.htn.R;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;


@FragmentWithArgs
public class ReportDialog extends DialogFragment {

    public interface Listener {
        void onRuleSubmitted(String rule);
        void onSiteRuleSubmitted(String rule);
        void onOtherSubmitted(String reason);
    }

    public static final String TAG = ReportDialog.class.getSimpleName();
    public static final String EXTRA_RULE = "EXTRA_RULE";
    public static final String EXTRA_SITE_RULE = "EXTRA_SITE_RULE";
    public static final String EXTRA_OTHER = "EXTRA_OTHER";

    @Arg String[] mRules;
    @Arg String[] mSiteRules;

    Listener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof Listener)) {
            throw new RuntimeException("Context must implement ReportDialog.Listener");
        }
        mListener = (Listener) context;
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Concatenate all options into single array
        String[] reportOptions = new String[mRules.length + mSiteRules.length];
        System.arraycopy(mRules, 0, reportOptions, 0, mRules.length);
        System.arraycopy(mSiteRules, 0, reportOptions, mRules.length, mSiteRules.length);

        // Create dialog with options
        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.report_menu_title)
                .setSingleChoiceItems(reportOptions, -1, null)
                .setPositiveButton(R.string.report_submit, onSubmit())
                .setNegativeButton(R.string.report_cancel, onCancel())
                .create();
    }

    DialogInterface.OnClickListener onSubmit() {
        return (dialogInterface, which) -> {
            if (which < mRules.length) {
                submit(mRules[which], null, null);
            } else if (which < mRules.length + mSiteRules.length) {
                submit(null, mSiteRules[which - mRules.length], null);
            } else {
                // TODO Support "other" reports
                submit(null, null, "");
            }
        };
    }

    DialogInterface.OnClickListener onCancel() {
        return (dialogInterface, which) -> dismiss();
    }

    private void submit(@Nullable String rule, @Nullable String siteRule, @Nullable String other) {
        Intent data = new Intent();

        if (rule != null) {
            mListener.onRuleSubmitted(rule);
            dismiss();
        }

        if (siteRule != null) {
            mListener.onSiteRuleSubmitted(siteRule);
            dismiss();
        }

        if (other != null) {
            mListener.onOtherSubmitted(other);
            dismiss();
        }
    }
}
