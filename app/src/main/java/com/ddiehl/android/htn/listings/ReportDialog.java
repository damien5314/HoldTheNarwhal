package com.ddiehl.android.htn.listings;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.ddiehl.android.htn.R;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public class ReportDialog extends DialogFragment {

    public static final String EXTRA_OPTION = "EXTRA_OPTION";

    String[] mReportOptions;
    String[] mReportOptionValues;

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.report_menu_title)
                .setSingleChoiceItems(mReportOptions, -1, null)
                .setPositiveButton(R.string.report_submit, onSubmit())
                .setNegativeButton(R.string.report_cancel, onCancel())
                .create();
    }

    DialogInterface.OnClickListener onSubmit() {
        return (dialogInterface, which) -> {
            String option = mReportOptionValues[which];
            finish(option);
        };
    }

    DialogInterface.OnClickListener onCancel() {
        return (dialogInterface, which) -> finish(RESULT_CANCELED, null);
    }

    private void finish(@NonNull String sort) {
        Intent data = new Intent();
        data.putExtra(EXTRA_OPTION, sort);
        finish(RESULT_OK, data);
    }

    private void finish(int resultCode, Intent data) {
        if (getTargetFragment() != null) {
            getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, data);
            dismiss();
        } else {
            getActivity().setResult(resultCode);
            getActivity().finish();
        }
    }
}
