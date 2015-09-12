/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view.dialogs;

import android.app.Activity;
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

    private Callbacks mListener;

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_sign_out_title)
                .setMessage(R.string.dialog_sign_out_message)
                .setPositiveButton(R.string.dialog_sign_out_ok, (dialog, which) -> {
                    if (mListener != null) {
                        mListener.onSignOutConfirm();
                    }
                })
                .setNegativeButton(R.string.dialog_sign_out_cancel, (dialog, which) -> {
                    if (mListener != null) {
                        mListener.onSignOutCancel();
                    }
                })
        .setCancelable(true);

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ConfirmSignOutDialog.Callbacks");
        }
    }
}
