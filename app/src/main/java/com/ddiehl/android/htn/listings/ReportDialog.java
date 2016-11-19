package com.ddiehl.android.htn.listings;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.ddiehl.android.htn.R;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;


@FragmentWithArgs
public class ReportDialog extends DialogFragment {

    public static final String TAG = ReportDialog.class.getSimpleName();

    public interface Listener {
        void onRuleSubmitted(String rule);
        void onSiteRuleSubmitted(String rule);
        void onOtherSubmitted(String reason);
        void onCancelled();
    }

    @Arg String[] mRules;
    @Arg String[] mSiteRules;

    int mSelectedIndex;
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentArgs.inject(this);

        if (mRules == null) {
            mRules = new String[0];
        }

        if (mSiteRules == null) {
            mSiteRules = new String[0];
        }
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
                .setSingleChoiceItems(reportOptions, -1, (dialogInterface, index) -> {
                    mSelectedIndex = index;
                })
                .setPositiveButton(R.string.report_submit, onSubmit())
                .setNegativeButton(R.string.report_cancel, onCancel())
                .create();
    }

    DialogInterface.OnClickListener onSubmit() {
        return (dialogInterface, which) -> {
            // If index is in rules array, submit the rule
            if (mSelectedIndex < mRules.length) {
                submit(mRules[mSelectedIndex], null, null);
            }
            // If index is in site rules array, submit the site rule
            else if (mSelectedIndex < mRules.length + mSiteRules.length) {
                submit(null, mSiteRules[mSelectedIndex - mRules.length], null);
            }
            // Otherwise, submit the other reason
            else {
                // TODO Support "other" reports
                submit(null, null, "");
            }
        };
    }

    DialogInterface.OnClickListener onCancel() {
        return (dialogInterface, which) -> {
            mListener.onCancelled();
            dismiss();
        };
    }

    private void submit(@Nullable String rule, @Nullable String siteRule, @Nullable String other) {
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
