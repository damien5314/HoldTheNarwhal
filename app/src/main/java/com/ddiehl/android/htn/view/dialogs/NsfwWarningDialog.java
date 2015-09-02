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

public class NsfwWarningDialog extends DialogFragment {

    public NsfwWarningDialog() { }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_nsfw_title)
                .setMessage(R.string.dialog_nsfw_message)
                .setPositiveButton(R.string.dialog_nsfw_confirm, (dialog, which) -> {
                    getTargetFragment().onActivityResult(getTargetRequestCode(),
                            Activity.RESULT_OK, null);
                })
                .setNegativeButton(R.string.dialog_nsfw_decline, (dialog, which) -> {
                    getTargetFragment().onActivityResult(getTargetRequestCode(),
                            Activity.RESULT_CANCELED, null);
                })
                .create();
    }
}
