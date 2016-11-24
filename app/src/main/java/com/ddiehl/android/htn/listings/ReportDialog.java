package com.ddiehl.android.htn.listings;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.ddiehl.android.htn.R;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import timber.log.Timber;

import static butterknife.ButterKnife.findById;


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

    int mSelectedIndex = -1;
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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Concatenate all options into single array
        String[] reportOptions = new String[mRules.length + mSiteRules.length];
        System.arraycopy(mRules, 0, reportOptions, 0, mRules.length);
        System.arraycopy(mSiteRules, 0, reportOptions, mRules.length, mSiteRules.length);
        final int numOptions = reportOptions.length;
        Timber.d("Report options passed: " + numOptions);

        // Create dialog with options
        LayoutInflater inflater = LayoutInflater.from(getContext());

        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.report_dialog_view, null, false);
        final RadioGroup parent = findById(view, R.id.dialog_view_group);
//        ViewGroup parent = findById(view, R.id.dialog_view_group);

        // Add 'other' dialog item
        View otherOptionView = inflater.inflate(R.layout.report_dialog_view_edit_item, parent, false);
        RadioButton otherSelector = findById(otherOptionView, R.id.report_choice_item_selector);

        // Add rest of option views
        for (int i = 0; i < numOptions; i++) {
            View optionView = inflater.inflate(R.layout.report_dialog_view_choice_item, parent, false);

            RadioButton selector = findById(optionView, R.id.report_choice_item_selector);
            selector.setId(i);

            // Set checked state change listener that caches selected index
            final int index = i;
            selector.setOnCheckedChangeListener((buttonView, isChecked) -> {
                parent.clearCheck();
                otherSelector.setChecked(false);
                if (isChecked) {
                    mSelectedIndex = index;
                }
            });

            // Set text for option
            selector.setText(reportOptions[i]);

            // Add view to parent
            parent.addView(optionView);
        }

        otherSelector.setId(numOptions);
        otherSelector.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Timber.d("Checked the 'other' option");
            parent.clearCheck();
            if (isChecked) {
                mSelectedIndex = numOptions;
                otherSelector.setChecked(true);
            }
        });

        parent.addView(otherOptionView);

        // Build AlertDialog from custom view
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.report_menu_title)
                .setPositiveButton(R.string.report_submit, onSubmit())
                .setNegativeButton(R.string.report_cancel, onCancelButton())
                .setView(view)
                .create();

        return dialog;
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
                EditText otherText = findById(getDialog(), R.id.report_choice_edit_text);
                String input = otherText.getText()
                        .toString()
                        .trim();
                submit(null, null, input);
            }
        };
    }

    DialogInterface.OnClickListener onCancelButton() {
        return (dialogInterface, index) -> onCancel(dialogInterface);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        mListener.onCancelled();
    }

    private void submit(@Nullable String rule, @Nullable String siteRule, @Nullable String other) {
        if (rule != null) {
            Toast.makeText(getContext(), "rule: " + rule, Toast.LENGTH_SHORT).show();
//            mListener.onRuleSubmitted(rule);
            dismiss();
        }

        if (siteRule != null) {
            Toast.makeText(getContext(), "site rule: " + siteRule, Toast.LENGTH_SHORT).show();
//            mListener.onSiteRuleSubmitted(siteRule);
            dismiss();
        }

        if (other != null) {
            Toast.makeText(getContext(), "other: " + other, Toast.LENGTH_SHORT).show();
//            mListener.onOtherSubmitted(other);
            dismiss();
        }
    }
}
