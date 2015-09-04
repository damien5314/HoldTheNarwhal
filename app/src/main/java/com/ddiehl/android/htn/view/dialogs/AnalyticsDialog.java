/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.ddiehl.android.htn.R;

public class AnalyticsDialog extends DialogFragment {

    public interface Callbacks {
        void onAnalyticsAccepted();
        void onAnalyticsDeclined();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_analytics_title)
                .setMessage(R.string.dialog_analytics_message)
                .setNeutralButton(R.string.dialog_analytics_accept, (dialog, which) -> {
                    getTargetFragment().onActivityResult(getTargetRequestCode(),
                            Activity.RESULT_OK, null);
                })
                .setNegativeButton(R.string.dialog_analytics_decline, (dialog, which) -> {
                    getTargetFragment().onActivityResult(getTargetRequestCode(),
                            Activity.RESULT_CANCELED, null);
                })
                .create();
    }

}
