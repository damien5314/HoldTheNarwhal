package com.ddiehl.android.htn.navigation;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.ddiehl.android.htn.R;

import org.jetbrains.annotations.NotNull;

public class ConfirmSignOutDialog extends DialogFragment {

    public static final String TAG = ConfirmSignOutDialog.class.getSimpleName();
    private Callbacks listener;

    public interface Callbacks {

        void onSignOutConfirm();

        void onSignOutCancel();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (Callbacks) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass().getSimpleName()
                    + " must implement AnalyticsDialog.Callbacks");
        }
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_sign_out_title)
                .setMessage(R.string.dialog_sign_out_message)
                .setPositiveButton(R.string.dialog_sign_out_ok, (dialog, which) -> {
                    if (listener != null) {
                        listener.onSignOutConfirm();
                    }
                })
                .setNegativeButton(R.string.dialog_sign_out_cancel, (dialog, which) -> {
                    if (listener != null) {
                        listener.onSignOutCancel();
                    }
                })
                .setCancelable(true)
                .create();
    }
}
