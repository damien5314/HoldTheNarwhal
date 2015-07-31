/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.ddiehl.android.htn.R;

public class ConfirmSignOutDialog extends DialogFragment {

    public interface Callbacks {
        void onSignOutConfirm();
        void onSignOutCancel();
    }

    public ConfirmSignOutDialog() { }

    public static ConfirmSignOutDialog newInstance() {
        Bundle args = new Bundle();
        ConfirmSignOutDialog dialog = new ConfirmSignOutDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_sign_out_title)
                .setMessage(R.string.dialog_sign_out_message)
                .setPositiveButton(R.string.dialog_sign_out_ok, (dialog, which) -> {
                    if (getActivity() instanceof Callbacks) {
                        ((Callbacks) getActivity()).onSignOutConfirm();
                    }
                })
                .setNegativeButton(R.string.dialog_sign_out_cancel, (dialog, which) -> {
                    if (getActivity() instanceof Callbacks) {
                        ((Callbacks) getActivity()).onSignOutCancel();
                    }
                })
        .setCancelable(true);

        return builder.create();
    }
}
