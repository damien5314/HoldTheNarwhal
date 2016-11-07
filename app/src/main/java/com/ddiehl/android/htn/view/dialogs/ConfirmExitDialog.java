package com.ddiehl.android.htn.view.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.ddiehl.android.htn.R;

public class ConfirmExitDialog extends DialogFragment {

    public static final String TAG = ConfirmExitDialog.class.getSimpleName();

    public interface Callbacks {

        void onConfirmExit();

        void onCancelExit();
    }

    private Callbacks mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof Callbacks)) {
            throw new ClassCastException(context + " must implement ConfirmExitDialogs.Callbacks");
        }
        mListener = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getContext())
                .setMessage(R.string.confirm_exit_dialog_title)
                .setPositiveButton(R.string.confirm_exit_dialog_confirm,
                        (dialogInterface, which) -> mListener.onConfirmExit())
                .setNegativeButton(R.string.confirm_exit_dialog_cancel,
                        (dialogInterface, which) -> mListener.onCancelExit())
                .create();
    }
}
