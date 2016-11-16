package com.ddiehl.android.htn.navigation;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.ddiehl.android.htn.R;

public class ConfirmSignOutDialog extends DialogFragment {

    public static final String TAG = ConfirmSignOutDialog.class.getSimpleName();
    private Callbacks mListener;

    public interface Callbacks {

        void onSignOutConfirm();

        void onSignOutCancel();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (Callbacks) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass().getSimpleName()
                    + " must implement AnalyticsDialog.Callbacks");
        }
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_sign_out_title)
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
                .setCancelable(true)
                .create();
    }
}
